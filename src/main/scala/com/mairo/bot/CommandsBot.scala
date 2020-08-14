package com.mairo.bot

import cats.Monad
import cats.data.EitherT
import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import com.bot4s.telegram.methods.SendDocument
import com.bot4s.telegram.models.InputFile
import com.mairo.bot.ParentBot._
import com.mairo.spi.{FlowControlService, ProviderSet}
import com.mairo.utils.Flow
import io.chrisdavenport.log4cats.Logger

class CommandsBot[F[_] : Async : Timer : ContextShift : Monad : Logger](token: String, botVersion: String)(implicit ps: ProviderSet[F])
  extends ParentBot[F](token)
    with StartCommand
    with SelfCommand {

  onCommand(START_CMD) { implicit msg =>
    for {
      _ <- logCmdInvocation(START_CMD)
      res <- response(startCmdText(botVersion))
    } yield res
  }

  onCommand(SELF_CMD) { implicit msg =>
    for {
      _ <- logCmdInvocation(SELF_CMD)
      res <- response(selfCmdText)
    } yield res
  }

  onCommand(PLAYERS_CMD) { implicit msg =>
    for {
      _ <- logCmdInvocation(PLAYERS_CMD)
      players <- FlowControlService.invoke(msg)(ps.playersCmdSP)
      result <- response(players)
    } yield result
  }

  onCommand(STATS_CMD) { implicit msg =>
    withArgs { args =>
      for {
        _ <- logCmdInvocation(STATS_CMD)
        stats <- FlowControlService.invoke(msg, args)(ps.statsCmdSP)
        result <- response(stats)
      } yield result
    }
  }

  onCommand(LAST_CMD) { implicit msg =>
    withArgs { args =>
      for {
        _ <- logCmdInvocation(LAST_CMD)
        stats <- FlowControlService.invoke(msg, args)(ps.lastCmdSP)
        result <- response(stats)
      } yield result
    }
  }

  onCommand(ADD_ROUND_CMD) { implicit msg =>
    withArgs { args =>
      for {
        _ <- logCmdInvocation(ADD_ROUND_CMD)
        stats <- FlowControlService.invoke(msg, args)(ps.addRoundCmdSP)
        result <- response(stats)
      } yield result
    }
  }

  onCommand(LOAD_XLSX_REPORT) { implicit msg =>
    withArgs { args =>
      val result = (for {
        _ <- EitherT(Flow.fromF(logCmdInvocation(LOAD_XLSX_REPORT)))
        byteArr <- EitherT(FlowControlService.produceBinary(msg, args)(ps.loadXlsxReportCmdSP))
      } yield byteArr).value
      Monad[F].flatMap(result) {
        case Left(err) =>
          for {
            error <- FlowControlService.formatError(err)
            res <- response(error)
          } yield res
        case Right(byteArr) =>
          val doc = InputFile("statistics.xlsx", byteArr)
          request(SendDocument(msg.chat.id, doc, replyMarkup = defaultMarkup())).void
      }
    }
  }

}

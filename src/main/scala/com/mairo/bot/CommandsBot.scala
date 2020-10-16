package com.mairo.bot

import cats.data.EitherT
import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import cats.{Monad, MonadError}
import com.bot4s.telegram.methods.SendDocument
import com.bot4s.telegram.models.Message
import com.mairo.bot.ParentBot._
import com.mairo.dtos.CataClientIntputDtos.{AddRoundDto, FindLastRounds}
import com.mairo.dtos.CataClientOutputDtos.{UklRequest, UklResponse}
import com.mairo.services._
import com.mairo.utils.{CataClientSprayCodecs, Flow}
import io.chrisdavenport.log4cats.Logger

class CommandsBot[F[_] : Async : Timer : ContextShift : Monad : Logger](token: String,
                                                                        botVersion: String,
                                                                        argValidator: ArgValidator[F],
                                                                        statsService: StatsService[F],
                                                                        uklSender: UklSender[F])
                                                                       (implicit MT: MonadError[F, Throwable])
  extends ParentBot[F](token)
    with StartCommand
    with SelfCommand
    with CataClientSprayCodecs {

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
      _ <- uklSender.send(UklRequest("listPlayers", msg.messageId, msg.chat.id.toString))
    } yield ()
  }

  onCommand(ADD_ROUND_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(ADD_ROUND_CMD)
        _ <- argValidator.validateAddRoundArgs(args)
        body <- MT.pure(addRoundFormat.write(prepareAddRoundDto(args, msg)))
        _ <- uklSender.send(UklRequest("addRound", msg.messageId, msg.chat.id.toString, body))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(LAST_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(LAST_CMD)
        _ <- argValidator.validateSeasonArgs(args)
        body <- MT.pure(findLastRoundsFormat.write(prepareLastRoundArgs(args)))
        _ <- uklSender.send(UklRequest("findLastRounds", msg.messageId, msg.chat.id.toString, body))
      } yield ()
      handleError(result, msg)
    }
  }


  onCommand(STATS_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(STATS_CMD)
        stats <- statsService.makeShortStats(args)
      } yield stats
      handleResponse(result)
    }
  }


  onCommand(LOAD_XLSX_REPORT) { implicit msg =>
    withArgs { args =>
      val result = (for {
        _ <- EitherT(Flow.fromF(logCmdInvocation(LOAD_XLSX_REPORT)))
        inputFile <- EitherT(statsService.loadXlsxReport(args, msg))
      } yield inputFile).value
      Monad[F].flatMap(result) {
        case Left(err) =>
          for {
            error <- formatError(err)
            res <- response(error)
          } yield res
        case Right(file) =>
          request(SendDocument(msg.chat.id, file, replyMarkup = defaultMarkup())).void
      }
    }
  }

  private def handleError(fa: F[Unit], msg: Message): F[Unit] = {
    MT.handleErrorWith(fa)(err => uklSender.selfSend(UklResponse(msg.messageId, msg.chat.id.toString, err.getMessage)))
  }

  private def prepareAddRoundDto(args: Seq[String], msg: Message): AddRoundDto = {
    val winners = args.head.split("/")
    val losers = args.tail.head.split("/")
    AddRoundDto(winners(0), winners(1), losers(0), losers(1), args.size == 3, msg.from.fold("0")(_.id.toString))
  }

  private def prepareLastRoundArgs(args: Seq[String]): FindLastRounds = {
    args match {
      case Seq() => FindLastRounds(QuarterCalculator.currentQuarter)
      case Seq(s) => FindLastRounds(s)
      case Seq(s, q) => FindLastRounds(s, q.toInt)
    }
  }

}

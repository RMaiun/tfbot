package com.mairo.bot

import cats.Monad
import cats.data.EitherT
import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import com.bot4s.telegram.methods.SendDocument
import com.mairo.bot.ParentBot._
import com.mairo.services.{PlayersService, RoundsService, StatsService}
import com.mairo.utils.Flow
import io.chrisdavenport.log4cats.Logger

class CommandsBot[F[_] : Async : Timer : ContextShift : Monad : Logger](token: String,
                                                                        botVersion: String,
                                                                        playersService: PlayersService[F],
                                                                        roundsService: RoundsService[F],
                                                                        statsService: StatsService[F])
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
    val result = for {
      _ <- logCmdInvocation(PLAYERS_CMD)
      players <- playersService.listAllPlayers()
    } yield players
    handleResponse(result)
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

  onCommand(LAST_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(LAST_CMD)
        stats <- roundsService.findLastRounds(args)
      } yield stats
      handleResponse(result)
    }
  }

  onCommand(ADD_ROUND_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(ADD_ROUND_CMD)
        stats <- roundsService.addRound(args, msg)
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

}

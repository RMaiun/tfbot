package com.mairo.bot

import cats.Monad
import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import com.mairo.bot.ParentBot._
import com.mairo.spi.{FlowControlService, ProviderSet}
import com.typesafe.scalalogging.Logger

class CommandsBot[F[_] : Async : Timer : ContextShift : Monad](token: String, botVersion: String)(implicit ps: ProviderSet[F])
  extends ParentBot[F](token)
    with StartCommand
    with SelfCommand {
  val log = Logger(getClass)

  onCommand(START_CMD) { implicit msg =>
    response(startCmdText(botVersion))
  }

  onCommand(SELF_CMD) { implicit msg =>
    response(selfCmdText)
  }

  onCommand(PLAYERS_CMD) { implicit msg =>
    for {
      players <- FlowControlService.invoke(msg)(ps.playersCmdSP)
      result <- response(players)
    } yield result
  }

  onCommand(STATS_CMD) { implicit msg =>
    withArgs { args =>
      for {
        stats <- FlowControlService.invoke(msg, args)(ps.statsCmdSP)
        result <- response(stats)
      } yield result
    }
  }

  onCommand(LAST_CMD) { implicit msg =>
    withArgs { args =>
      for {
        stats <- FlowControlService.invoke(msg, args)(ps.lastCmdSP)
        result <- response(stats)
      } yield result
    }
  }

}

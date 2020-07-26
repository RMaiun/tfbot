package com.mairo

import cats.Monad
import cats.effect.{Async, Concurrent, ContextShift, Timer}
import cats.implicits._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.typesafe.scalalogging.Logger

/**
  * Showcases different ways to declare commands (Commands + RegexCommands).
  *
  * Note that non-ASCII commands are not clickable.
  *
  * @param token Bot's token.
  */
class CommandsBot[F[_] : Async : Timer : ContextShift : Concurrent](token: String, cc: CataClient[F])
                                                                   (implicit F: Monad[F])
  extends ExampleBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {
  val log = Logger(getClass)

  private def preparePlayers(p: Players): String = {
    p.players.map(x => s"${x.id}|${x.surname}").mkString("\n")
  }

  // '/' prefix is optional
  onCommand("hola") { implicit msg =>
    log.info("/hola TRIGGERED")
    for {
      resp <- cc.fetchPlayers()
      res = resp.fold(err => s"Error: $err", v => preparePlayers(v))
      z <- reply(res).void
    } yield
      z
  }
}

package com.mairo

import cats.Monad
import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.mairo.services.{CataClient, FlowControlService, ServiceProducer}
import com.typesafe.scalalogging.Logger

/**
  * Showcases different ways to declare commands (Commands + RegexCommands).
  *
  * Note that non-ASCII commands are not clickable.
  *
  * @param token Bot's token.
  */
class CommandsBot[F[_] : Async : Timer : ContextShift : Monad](token: String)(implicit cc: CataClient[F])
  extends ExampleBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {
  val log = Logger(getClass)


  // '/' prefix is optional
  onCommand("hola") { implicit msg =>
    for {
      bla <- FlowControlService.invoke(msg)(ServiceProducer.producer(cc))
      z <- reply(bla).void
    } yield z
  }

}

package com.mairo.spi

import cats.Monad
import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import com.bot4s.telegram.models.Message
import com.mairo.exceptions.BotException.{CataclysmExpectedException, CataclysmUnexpectedException}
import com.typesafe.scalalogging.LazyLogging

object FlowControlService extends LazyLogging {

  def invoke[F[_] : Monad : Async, A](msg: Message, args: Seq[String] = Seq())
                                     (serviceProducer: ServiceProvider[F, A]): F[String] = {

    val executionResult = for {
      _ <- EitherT(serviceProducer.validator.validate(args))
      dto <- EitherT(serviceProducer.processor.process(msg, args))
      result <- EitherT(serviceProducer.formatter.format(dto))
    } yield result

    Monad[F].flatMap(executionResult.value) {
      case Left(err) => formatError(err)
      case Right(v)
      => Monad[F].pure(v)
    }
  }

  private def formatError[F[_] : Async : Monad](err: Throwable): F[String] = {
    val str = err match {
      case e: CataclysmExpectedException => s"*ERROR*: ${e.getMessage}"
      case e: CataclysmUnexpectedException => s"*ERROR*: ${e.getMessage}"
      case _ => s"*ERROR*: ${err.getMessage}"
    }
    for {
      _ <- Async[F].delay(err.printStackTrace())
      error <- Monad[F].pure(str)
    } yield error
  }
}

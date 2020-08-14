package com.mairo.spi

import cats.Monad
import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import com.bot4s.telegram.models.Message
import com.mairo.exceptions.BotException.{CataclysmExpectedException, CataclysmUnexpectedException}
import com.mairo.utils.Flow.{BinaryFlow, Flow}

object FlowControlService {

  def invoke[F[_] : Monad : Async, A](msg: Message, args: Seq[String] = Seq())
                                     (serviceProvider: ServiceProvider[F, A]): F[String] = {

    val executionResult = for {
      _ <- EitherT(serviceProvider.validator.validate(args))
      dto <- EitherT(serviceProvider.processor.process(msg, args))
      result <- EitherT(serviceProvider.formatter.format(dto))
    } yield result

    Monad[F].flatMap(executionResult.value) {
      case Left(err) => formatError(err)
      case Right(v)
      => Monad[F].pure(v)
    }
  }

  def produceBinary[F[_] : Monad : Async](msg: Message, args: Seq[String] = Seq())
                                     (serviceProducer: BinaryServiceProvider[F]): BinaryFlow[F] = {

    val executionResult = for {
      _ <- EitherT(serviceProducer.validator.validate(args))
      byteArr <- EitherT(serviceProducer.processor.process(msg, args))
    } yield byteArr
    executionResult.value
    }

  def formatError[F[_] : Async : Monad](err: Throwable): F[String] = {
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

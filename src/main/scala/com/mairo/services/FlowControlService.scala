package com.mairo.services

import cats.Monad
import cats.data.EitherT
import com.bot4s.telegram.models.Message
import com.typesafe.scalalogging.LazyLogging

object FlowControlService extends LazyLogging {

  def invoke[F[_] : Monad, A](msg: Message, args: Seq[String] = Seq())
                             (serviceProducer: ServiceProducer[F, A]): F[String] = {

    val executionResult = for {
      _ <- EitherT(serviceProducer.validator.validate(args))
      dto <- EitherT(serviceProducer.processor.process(msg, args))
      result <- EitherT(serviceProducer.formatter.format(dto))
    } yield result

    Monad[F].flatMap(executionResult.value) {
      case Left(err) => Monad[F].pure(formatError(err))
      case Right(v) => Monad[F].pure(v)
    }
  }

  private def formatError(e: Throwable): String = {
    e.printStackTrace()
    s"*ERROR*: ${e.getMessage}"
  }
}

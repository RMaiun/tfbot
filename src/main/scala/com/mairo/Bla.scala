package com.mairo

import cats.Monad
import cats.data.EitherT
import com.bot4s.telegram.models.Message
import com.mairo.utils.Aliases.Flow

trait ArgValidator[F[_]] {
  def validate(args: Seq[String]): Flow[F, Unit]
}

trait CmdProcessor[F[_], A] {
  def process(msg: Message, args: Seq[String] = Seq()): Flow[F, A]
}

trait MessageFormatter[F[_], A] {
  def format(data: A): Flow[F, String]
}

trait ServiceProducer[F[_], A] {
  def validator: ArgValidator[F]

  def processor: CmdProcessor[F, A]

  def formatter: MessageFormatter[F, A]
}


object FlowControl {
  def invoke[F[_] : Monad, A](msg: Message, args: Seq[String] = Seq())(serviceProducer: ServiceProducer[F, A]): Flow[F, String] = {

    val executionResult = for {
      _ <- EitherT(serviceProducer.validator.validate(args))
      dto <- EitherT(serviceProducer.processor.process(msg, args))
      result <- EitherT(serviceProducer.formatter.format(dto))
    } yield result
    executionResult.value
  }
}


package com.mairo.services

import cats.Monad
import cats.effect.ContextShift
import com.mairo.dtos.CataClientDtos.Players

case class ServiceProducer[F[_], A](validator: ArgValidator[F],
                                    processor: CmdProcessor[F, A],
                                    formatter: MessageFormatter[F, A])


object ServiceProducer {
  def producer[F[_] : Monad : ContextShift](cc: CataClient[F]): ServiceProducer[F, Players] =
    new ServiceProducer[F, Players](
      ArgValidator.EmptyValidator(),
      CmdProcessor.PlayersCmdProcessor()(cc),
      MessageFormatter.PlayersCmdFormatter())
}
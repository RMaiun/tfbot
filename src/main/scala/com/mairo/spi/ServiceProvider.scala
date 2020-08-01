package com.mairo.spi

import cats.Monad
import cats.effect.ContextShift
import com.mairo.dtos.CataClientDtos.{FoundLastRounds, Players, ShortInfoStats}
import com.mairo.services.CataClient

case class ServiceProvider[F[_], A](validator: ArgValidator[F],
                                    processor: CmdProcessor[F, A],
                                    formatter: MessageFormatter[F, A])


object ServiceProvider {
  def playersCmdServiceProvider[F[_] : Monad : ContextShift](cc: CataClient[F]): ServiceProvider[F, Players] =
    new ServiceProvider[F, Players](
      ArgValidator.EmptyValidator(),
      CmdProcessor.PlayersCmdProcessor(cc),
      MessageFormatter.PlayersCmdFormatter())

  def statsCmdServiceProvider[F[_] : Monad : ContextShift](cc: CataClient[F]): ServiceProvider[F, ShortInfoStats] =
    new ServiceProvider[F, ShortInfoStats](
      ArgValidator.StatsValidator(),
      CmdProcessor.StatsCmdProcessor(cc),
      MessageFormatter.StatsCmdFormatter())

  def lastCmdServiceProvider[F[_] : Monad : ContextShift](cc: CataClient[F]): ServiceProvider[F, FoundLastRounds] =
    new ServiceProvider[F, FoundLastRounds](
      ArgValidator.LastCmdValidator(),
      CmdProcessor.LastCmdProcessor(cc),
      MessageFormatter.LastCmdFormatter())
}
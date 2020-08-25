package com.mairo.services

import cats.Monad
import cats.data.EitherT
import cats.effect.ContextShift
import com.mairo.utils.Flow.Flow
import io.chrisdavenport.log4cats.Logger

class PlayersService[F[_] : ContextShift : Monad : Logger](cc: CataClient[F]) {

  def listAllPlayers(): Flow[F, String] = {
    val result = for {
      players <- EitherT(cc.fetchPlayers())
      str <- EitherT(MessageFormatter.formatPlayers(players))
    } yield str
    result.value
  }

}

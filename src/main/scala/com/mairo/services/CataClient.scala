package com.mairo.services

import cats.MonadError
import cats.effect.ContextShift
import cats.implicits._
import com.mairo.dtos.CataClientDtos.Players
import com.mairo.utils.Flow.Flow
import com.mairo.utils.SprayCodecs
import com.softwaremill.sttp._
import com.softwaremill.sttp.sprayJson._
import com.typesafe.scalalogging.LazyLogging

class CataClient[F[_] : ContextShift](implicit be: SttpBackend[F, Nothing], F: MonadError[F, Throwable])
  extends SprayCodecs with LazyLogging {

  def fetchPlayers(): Flow[F, Players] = {
    logger.info("Sending request to cataclysm /players/all")
    val request = sttp.get(uri"http://localhost:8080/players/all")
      .response(asJson[Players])
    val response: Flow[F, Players] = be.send(request)
      .map(_.body)
      .map(_.left.map(str => new RuntimeException(str)))
    F.handleError(response)(err => err.asLeft[Players])
  }
}

package com.mairo.services

import cats.MonadError
import cats.effect.ContextShift
import cats.implicits._
import com.mairo.dtos.CataClientDtos.{FoundLastRounds, Players, ShortInfoStats}
import com.mairo.utils.Flow.Flow
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}
import com.softwaremill.sttp._
import com.softwaremill.sttp.sprayJson._
import com.typesafe.scalalogging.LazyLogging

class CataClient[F[_] : ContextShift](implicit be: SttpBackend[F, Nothing], F: MonadError[F, Throwable])
  extends CataClientSprayCodecs with LazyLogging with AppConfig {

  def fetchPlayers(): Flow[F, Players] = {
    val path = s"http://localhost:8080/players/all"
    logger.info("Sending request to cataclysm {}", path)
    val request = sttp.get(uri"$path")
      .response(asJson[Players])
    val response: Flow[F, Players] = be.send(request)
      .map(_.body)
      .map(_.left.map(str => new RuntimeException(str)))
    F.handleError(response)(err => err.asLeft[Players])
  }

  def fetchShortStats(season: String): Flow[F, ShortInfoStats] = {
    val path = s"http://localhost:8080/stats/short/${season.toUpperCase}"
    logger.info("Sending request to cataclysm {}", path)
    val request = sttp.get(uri"$path")
      .response(asJson[ShortInfoStats])
    val response: Flow[F, ShortInfoStats] = be.send(request)
      .map(_.body)
      .map(_.left.map(str => new RuntimeException(str)))
    F.handleError(response)(err => err.asLeft[ShortInfoStats])
  }

  def fetchLastRounds(season: String, qty: Option[Int] = None): Flow[F, FoundLastRounds] = {
    val roundsNum = qty.fold(lastRoundsQty)(x => x)
    val path = s"http://localhost:8080/rounds/findLast/${season.toUpperCase}/$roundsNum"
    logger.info("Sending request to cataclysm {}", path)
    val request = sttp.get(uri"$path")
      .response(asJson[FoundLastRounds])
    val response: Flow[F, FoundLastRounds] = be.send(request)
      .map(_.body)
      .map(_.left.map(str => new RuntimeException(str)))
    F.handleError(response)(err => err.asLeft[FoundLastRounds])
  }
}

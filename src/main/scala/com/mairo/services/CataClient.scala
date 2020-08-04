package com.mairo.services

import cats.MonadError
import cats.effect.ContextShift
import cats.implicits._
import com.mairo.dtos.CataClientIntputDtos.AddRoundDto
import com.mairo.dtos.CataClientOutputDtos.{FoundLastRounds, Players, ShortInfoStats, StoredId}
import com.mairo.exceptions.BotException.CataclysmExpectedException
import com.mairo.utils.Flow.Flow
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}
import com.softwaremill.sttp._
import com.softwaremill.sttp.sprayJson._
import io.chrisdavenport.log4cats.Logger

class CataClient[F[_] : ContextShift : Logger](implicit be: SttpBackend[F, Nothing], F: MonadError[F, Throwable])
  extends CataClientSprayCodecs with AppConfig {

  private def sendRequest[T](path: String, request: RequestT[Id, T, Nothing]): Flow[F, T] = {
    val response = for {
      _ <- logRequest(path)
      resp <- be.send(request)
    } yield resp
    handleResponse(response)
  }

  private def handleResponse[T](resp: F[Response[T]]): Flow[F, T] = {
    val mappedResponse: Flow[F, T] = resp.map(_.body)
      .map(_.left.map(str => CataclysmExpectedException(str)))
    F.handleError(mappedResponse)(err => err.asLeft[T])
  }

  private def logRequest(path: String): F[Unit] = {
    Logger[F].debug(s"Sending request to cataclysm $path")
  }

  def fetchPlayers(): Flow[F, Players] = {
    val path = s"$cataclysmRoot/players/all"
    val request: RequestT[Id, Players, Nothing] = sttp.get(uri"$path")
      .response(asJson[Players])
    sendRequest(path, request)
  }

  def fetchShortStats(season: String): Flow[F, ShortInfoStats] = {
    val path = s"$cataclysmRoot/stats/short/${season.toUpperCase}"
    val request = sttp.get(uri"$path")
      .response(asJson[ShortInfoStats])
    sendRequest(path, request)
  }

  def fetchLastRounds(season: String, qty: Option[Int] = None): Flow[F, FoundLastRounds] = {
    val roundsNum = qty.fold(lastRoundsQty)(x => x)
    val path = s"$cataclysmRoot/rounds/findLast/${season.toUpperCase}/$roundsNum"
    val request = sttp.get(uri"$path")
      .response(asJson[FoundLastRounds])
    sendRequest(path, request)
  }

  def addRound(dto: AddRoundDto): Flow[F, StoredId] = {
    val path = s"$cataclysmRoot/rounds/add"
    val request = sttp.post(uri"$path")
      .body(dto)
      .response(asJson[StoredId])
    sendRequest(path, request)
  }
}

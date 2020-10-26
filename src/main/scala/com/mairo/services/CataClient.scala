package com.mairo.services

import cats.MonadError
import cats.effect.{ContextShift, Timer}
import cats.implicits._
import com.mairo.exceptions.BotException.CataclysmExpectedException
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}
import com.softwaremill.sttp._
import io.chrisdavenport.log4cats.Logger

class CataClient[F[_] : ContextShift : Logger](implicit be: SttpBackend[F, Nothing],
                                               timer: Timer[F],
                                               F: MonadError[F, Throwable])
  extends CataClientSprayCodecs with AppConfig {

  def getStatsXlsxDocument(season: String): F[Array[Byte]] = {
    val path = s"$cataclysmRoot/reports/xlsx/$season"
    val request: RequestT[Id, Array[Byte], Nothing] = sttp.get(uri"$path")
      .response(asByteArray)
    sendRequest(path, request)
  }

  private def sendRequest[T](path: String, request: RequestT[Id, T, Nothing]): F[T] = {
    val response = for {
      _ <- logRequest(path)
      resp <- be.send(request)
    } yield resp
    handleResponse(response)
  }

  private def handleResponse[T](resp: F[Response[T]]): F[T] = {
    resp.map(_.body)
      .flatMap {
        case Left(str) => F.raiseError(CataclysmExpectedException(str))
        case Right(data) => F.pure(data)
      }
  }

  private def logRequest(path: String): F[Unit] = {
    Logger[F].debug(s"Sending request to cataclysm $path")
  }

}

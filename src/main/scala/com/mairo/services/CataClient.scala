package com.mairo.services

import java.util.UUID

import cats.MonadError
import cats.effect.{ContextShift, Timer}
import cats.implicits._
import com.bot4s.telegram.models.Message
import com.mairo.exceptions.BotException.CataclysmExpectedException
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}
import com.softwaremill.sttp._
import io.chrisdavenport.log4cats.Logger

class CataClient[F[_] : ContextShift : Logger](implicit be: SttpBackend[F, Nothing],
                                               timer: Timer[F],
                                               F: MonadError[F, Throwable])
  extends CataClientSprayCodecs with AppConfig {

  case class FileResponse(name: String, data: BinaryFile)

  type BinaryFile = Array[Byte]

  def getStatsXlsxDocument(season: String): F[FileResponse] = {
    val path = s"$cataclysmRoot/reports/xlsx/$season"
    val request: RequestT[Id, Array[Byte], Nothing] = sttp.get(uri"$path")
      .response(asByteArray)
    sendRequest(path, request)
  }

  def getDump(implicit msg: Message): F[FileResponse] = {
    val moderator = msg.from.fold("0")(_.id.toString)
    val path = s"$cataclysmRoot/dump/export/$moderator"
    val request: RequestT[Id, Array[Byte], Nothing] = sttp.get(uri"$path")
      .response(asByteArray)
    sendRequest(path, request)
  }

  private def sendRequest(path: String, request: RequestT[Id, BinaryFile, Nothing]): F[FileResponse] = {
    val response = for {
      _ <- logRequest(path)
      resp <- be.send(request)
      fileName <- fileName(resp)
    } yield (fileName, resp)
    handleResponse(response)
  }

  private def fileName(resp: Response[BinaryFile]): F[String] = {
    F.pure(resp.headers
      .find(p => p._1.toLowerCase == HeaderNames.ContentDisposition.toLowerCase())
      .map(_._2)
      .fold(UUID.randomUUID().toString)(x => x.split(";")(1).trim.replace("filename=", "")))
  }

  private def handleResponse(resp: F[(String, Response[BinaryFile])]): F[FileResponse] = {
    resp.flatMap(x => prepareBody(x._1, x._2.body))

  }

  private def prepareBody(name: String, data: Either[String, BinaryFile]): F[FileResponse] = {
    data match {
      case Left(str) => F.raiseError(CataclysmExpectedException(str))
      case Right(value) => F.pure(FileResponse(name, value))
    }
  }

  private def logRequest(path: String): F[Unit] = {
    Logger[F].debug(s"Sending request to cataclysm $path")
  }

}

package com.mairo

import cats.effect.{ExitCode, IO, IOApp}
import com.mairo.utils.IoSttpBackend
import com.softwaremill.sttp.SttpBackend
import com.typesafe.scalalogging.LazyLogging

object Launcher extends IOApp with LazyLogging {
  implicit val client: SttpBackend[IO, Nothing] = new IoSttpBackend()
  implicit val cc = new IoSttpBackend()

  def run(args: List[String]): IO[ExitCode] = {
    logger.info("TFBOT have started successfully")
    IO("")
    val client = new CataClient[IO]
    new CommandsBot[IO]("", client)
      .startPolling()
      .map(_ => ExitCode.Success)
  }
}
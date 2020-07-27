package com.mairo

import cats.effect.{ExitCode, IO, IOApp}
import com.mairo.services.CataClient
import com.mairo.utils.IoSttpBackend
import com.softwaremill.sttp.SttpBackend
import com.typesafe.scalalogging.LazyLogging

object Launcher extends IOApp with LazyLogging {
  implicit val be: SttpBackend[IO, Nothing] = new IoSttpBackend()
  implicit val cataClient: CataClient[IO] = new CataClient[IO]

  def run(args: List[String]): IO[ExitCode] = {
    logger.info("TFBOT have started successfully")
    IO("")
    new CommandsBot[IO]("")
      .startPolling()
      .map(_ => ExitCode.Success)
  }
}
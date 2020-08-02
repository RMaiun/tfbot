package com.mairo

import cats.effect.{ExitCode, IO, IOApp}
import com.mairo.bot.CommandsBot
import com.mairo.services.CataClient
import com.mairo.spi.{ProviderSet, ServiceProvider}
import com.mairo.utils.{AppConfig, IoSttpBackend}
import com.softwaremill.sttp.SttpBackend
import com.typesafe.scalalogging.LazyLogging

object Launcher extends IOApp with LazyLogging with AppConfig {
  implicit val be: SttpBackend[IO, Nothing] = new IoSttpBackend()
  val cataClient: CataClient[IO] = new CataClient[IO]

  implicit val serviceProviders: ProviderSet[IO] = spi.ProviderSet(
    ServiceProvider.playersCmdServiceProvider(cataClient),
    ServiceProvider.statsCmdServiceProvider(cataClient),
    ServiceProvider.lastCmdServiceProvider(cataClient),
    ServiceProvider.addRoundCmdServiceProvider(cataClient))

  logger.info("Found bot version = {}", botVersion)
  logger.info("Found bot token = {}", botToken)
  logger.info("Found cataclysm.root = {}", cataclysmRoot)

  def run(args: List[String]): IO[ExitCode] = {
    logger.info("TFBOT have started successfully")
    new CommandsBot[IO](botToken, botVersion)
      .startPolling()
      .map(_ => ExitCode.Success)
  }
}
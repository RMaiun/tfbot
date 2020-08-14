package com.mairo

import cats.effect.{ExitCode, IO, IOApp, Sync}
import com.mairo.bot.CommandsBot
import com.mairo.services.CataClient
import com.mairo.spi.{ProviderSet, ServiceProvider}
import com.mairo.utils.{AppConfig, IoSttpBackend}
import com.softwaremill.sttp.SttpBackend
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.{Logger, SelfAwareStructuredLogger}

object Launcher extends IOApp with AppConfig {
  implicit val be: SttpBackend[IO, Nothing] = new IoSttpBackend()

  implicit def unsafeLogger[F[_] : Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  implicit val cataClient: CataClient[IO] = new CataClient[IO]

  implicit val serviceProviders: ProviderSet[IO] = spi.ProviderSet(
    ServiceProvider.playersCmdServiceProvider(),
    ServiceProvider.statsCmdServiceProvider(),
    ServiceProvider.lastCmdServiceProvider(),
    ServiceProvider.addRoundCmdServiceProvider(),
    ServiceProvider.loadXlsxReportServiceProvider())

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info(s"Found bot version = $botVersion")
      _ <- Logger[IO].info(s"Found bot token = $botToken")
      _ <- Logger[IO].info(s"Found cataclysm.root = $cataclysmRoot")
      _ <- Logger[IO].info("TFBOT have started successfully")
      exitCode <- new CommandsBot[IO](botToken, botVersion).startPolling().map(_ => ExitCode.Success)
    } yield exitCode
  }
}
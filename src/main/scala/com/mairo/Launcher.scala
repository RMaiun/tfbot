package com.mairo

import cats.effect.{ExitCode, IO, IOApp, Sync}
import com.mairo.bot.CommandsBot
import com.mairo.services._
import com.mairo.utils.AppConfig
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.{Logger, SelfAwareStructuredLogger}

object Launcher extends IOApp with AppConfig {
  implicit val be: SttpBackend[IO, Nothing] = AsyncHttpClientCatsBackend[IO]()

  implicit def unsafeLogger[F[_] : Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val cataClient: CataClient[IO] = new CataClient[IO]
  val argValidator: ArgValidator[IO] = new ArgValidator[IO]()
  val sender = new UklSender[IO]
  val bot = new CommandsBot[IO](botToken, botVersion, argValidator, sender, cataClient)
  val consumer = new UklConsumer[IO](bot)

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info(s"Found bot version = $botVersion")
      _ <- Logger[IO].info(s"Found bot token = $botToken")
      _ <- Logger[IO].info(s"Found cataclysm.root = $cataclysmRoot")
      _ <- Logger[IO].info("TFBOT have started successfully")
      _ <- Sync[IO].delay(consumer.startListener())
      exitCode <- bot.startPolling().map(_ => ExitCode.Success)
    } yield exitCode
  }
}
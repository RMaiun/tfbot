package com.mairo.services

import cats.Monad
import cats.data.EitherT
import cats.effect.ContextShift
import com.bot4s.telegram.models.{InputFile, Message}
import com.mairo.services.ArgValidator.validateSeasonArgs
import com.mairo.utils.Flow.Flow
import io.chrisdavenport.log4cats.Logger

class StatsService[F[_] : ContextShift : Monad : Logger](cc: CataClient[F]) {

  def makeShortStats(args: Seq[String]): Flow[F, String] = {
    val result = for {
      _ <- EitherT(validateSeasonArgs(args))
      season = if (args.isEmpty) QuarterCalculator.currentQuarter else args.head
      stats <- EitherT(cc.fetchShortStats(season))
      str <- EitherT(MessageFormatter.formatShortInfoStats(stats))
    } yield str
    result.value
  }

  def loadXlsxReport(args: Seq[String], msg: Message): Flow[F, InputFile] = {
    val result = for {
      _ <- EitherT(validateSeasonArgs(args))
      season = if (args.isEmpty) QuarterCalculator.currentQuarter else args.head
      byteArr <- EitherT(cc.getStatsXlsxDocument(season))
    } yield InputFile(s"$season-statistics.xlsx", byteArr)
    result.value
  }
}

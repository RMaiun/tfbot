package com.mairo.services

import cats.Monad
import cats.data.EitherT
import cats.effect.ContextShift
import com.bot4s.telegram.models.Message
import com.mairo.dtos.CataClientIntputDtos.AddRoundDto
import MessageFormatter._
import com.mairo.utils.Flow.Flow
import io.chrisdavenport.log4cats.Logger

class RoundsService[F[_] : ContextShift : Monad : Logger](cc: CataClient[F]) {

  def addRound(args: Seq[String], msg: Message): Flow[F, String] = {
    val result = for {
      _ <- EitherT(ArgValidator.validateAddRoundArgs(args))
      id <- EitherT(cc.addRound(prepareAddRoundDto(args, msg)))
      str <- EitherT(formatStoredId(id))
    } yield str
    result.value
  }

  def findLastRounds(args: Seq[String]): Flow[F, String] = {
    val result = for {
      _ <- EitherT(ArgValidator.validateSeasonArgs(args))
      dto = prepareLastRoundArgs(args)
      foundRounds <- EitherT(cc.fetchLastRounds(dto._1, dto._2))
      str <- EitherT(formatLastRounds(foundRounds))
    } yield str
    result.value
  }

  private def prepareLastRoundArgs(args: Seq[String]): (String, Option[Int]) = {
    args match {
      case Seq() => (QuarterCalculator.currentQuarter, None)
      case Seq(s) => (s, None)
      case Seq(s, q) => (s, Some(q.toInt))
    }
  }

  private def prepareAddRoundDto(args: Seq[String], msg: Message): AddRoundDto = {
    val winners = args.head.split("/")
    val losers = args.tail.head.split("/")
    AddRoundDto(winners(0), winners(1), losers(0), losers(1), args.size == 3, msg.from.fold("0")(_.id.toString))
  }
}

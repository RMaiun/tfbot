package com.mairo.spi

import cats.Monad
import cats.effect.ContextShift
import com.bot4s.telegram.models.Message
import com.mairo.dtos.CataClientDtos.{FoundLastRounds, Players, ShortInfoStats}
import com.mairo.services.{CataClient, QuarterCalculator}
import com.mairo.utils.Flow.Flow

trait CmdProcessor[F[_], A] {
  def process(msg: Message, args: Seq[String] = Seq()): Flow[F, A]
}

object CmdProcessor {

  case class PlayersCmdProcessor[F[_] : Monad : ContextShift, A](cc: CataClient[F]) extends CmdProcessor[F, Players] {
    override def process(msg: Message, args: Seq[String]): Flow[F, Players] = cc.fetchPlayers()
  }

  case class StatsCmdProcessor[F[_] : Monad : ContextShift, A](cc: CataClient[F]) extends CmdProcessor[F, ShortInfoStats] {
    override def process(msg: Message, args: Seq[String]): Flow[F, ShortInfoStats] = args match {
      case Seq() => cc.fetchShortStats(QuarterCalculator.currentQuarter)
      case _ => cc.fetchShortStats(args.head)
    }
  }

  case class LastCmdProcessor[F[_] : Monad : ContextShift, A](cc: CataClient[F]) extends CmdProcessor[F, FoundLastRounds] {
    override def process(msg: Message, args: Seq[String]): Flow[F, FoundLastRounds] = {
      val (season, qty): (String, Option[Int]) = args match {
        case Seq() => (QuarterCalculator.currentQuarter, None)
        case Seq(s) => (s, None)
        case Seq(s, q) => (s, Some(q.toInt))
      }
      cc.fetchLastRounds(season, qty)
    }
  }

}

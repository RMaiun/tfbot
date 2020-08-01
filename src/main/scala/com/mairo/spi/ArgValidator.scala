package com.mairo.spi

import cats.Monad
import com.mairo.exceptions.BotException.InvalidArgsNumberException
import com.mairo.utils.Flow.Flow
import com.mairo.utils.{Flow, Validations}

trait ArgValidator[F[_]] {
  def validate(args: Seq[String]): Flow[F, Seq[String]]
}

object ArgValidator extends Validations {

  case class EmptyValidator[F[_] : Monad]() extends ArgValidator[F] {
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = Flow.right(args)
  }

  case class StatsValidator[F[_] : Monad]() extends ArgValidator[F] {
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = {
      args match {
        case Seq() => Flow.right(args)
        case Seq(season) => Monad[F].map(Flow.fromResult(isSeasonValid(season)))(res => res.map(Seq(_)))
        case _ => Flow.left[F, Seq[String]](InvalidArgsNumberException())
      }
    }
  }

  case class LastCmdValidator[F[_] : Monad]() extends ArgValidator[F] {
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = {
      args match {
        case Seq() => Flow.right(args)
        case Seq(season) => Monad[F].map(Flow.fromResult(isSeasonValid(season)))(res => res.map(Seq(_)))
        case Seq(season, qty) => validateSeasonWithQty(season, qty)
        case _ => Flow.left[F, Seq[String]](InvalidArgsNumberException())
      }
    }

    private def validateSeasonWithQty(season: String, qty: String): Flow[F, Seq[String]] = {
      val res = for {
        s <- isSeasonValid(season)
        q <- isInt(qty)
      } yield Seq(s, q)
      Flow.fromResult(res)
    }
  }

}
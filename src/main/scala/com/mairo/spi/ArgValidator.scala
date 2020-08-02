package com.mairo.spi

import cats.Monad
import com.mairo.exceptions.BotException.{InvalidArgsNumberException, WrongDefinedArgsNumberException, WrongShutoutArgException}
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

  case class AddRoundCmdValidator[F[_] : Monad]() extends ArgValidator[F] {
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = {
      val trimArgs = args.map(_.trim)
      trimArgs.size match {
        case 4 => Flow.right(trimArgs)
        case 5 =>
          if (args.last.trim == "суха") {
            Flow.right(trimArgs)
          } else {
            Flow.left(WrongShutoutArgException(args.last))
          }
        case _ => Flow.left(WrongDefinedArgsNumberException(4, args.size))
      }
    }
  }

}
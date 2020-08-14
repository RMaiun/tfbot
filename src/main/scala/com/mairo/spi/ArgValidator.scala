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

  class SeasonValidator[F[_] : Monad] extends ArgValidator[F]{
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = {
      args match {
        case Seq() => Flow.right(args)
        case Seq(season) => validateSeason(season)
        case _ => Flow.left[F, Seq[String]](InvalidArgsNumberException())
      }
    }
  }

  case class StatsValidator[F[_] : Monad]() extends SeasonValidator[F]
  case class LoadXlsxValidator[F[_] : Monad]() extends SeasonValidator[F]

  case class LastCmdValidator[F[_] : Monad]() extends ArgValidator[F] {
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = {
      args match {
        case Seq() => Flow.right(args)
        case Seq(season) => validateSeason(season)
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
        case 2 =>
          if (checkPlayerPairs(trimArgs)) {
            Flow.right(trimArgs)
          } else {
            Flow.left(InvalidArgsNumberException())
          }
        case 3 =>
          if (checkPlayerPairs(trimArgs) && trimArgs.last.trim == "суха") {
            Flow.right(trimArgs)
          } else {
            Flow.left(WrongShutoutArgException(args.last))
          }
        case _ => Flow.left(WrongDefinedArgsNumberException(4, args.size))
      }
    }

    private def checkPlayerPairs(args: Seq[String]): Boolean = {
      val wOk = args.head.contains("/")
      val lOk = args.tail.head.contains("/")
      wOk && lOk
    }
  }

}
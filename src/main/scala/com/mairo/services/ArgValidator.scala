package com.mairo.services

import cats.Monad
import com.mairo.exceptions.BotException.{InvalidArgsNumberException, WrongDefinedArgsNumberException, WrongShutoutArgException}
import com.mairo.utils.Flow.Flow
import com.mairo.utils.{Flow, Validations}

object ArgValidator extends Validations {

  def validateSeasonArgs[F[_] : Monad](args: Seq[String]): Flow[F, Seq[String]] = {
    args match {
      case Seq() => Flow.right(args)
      case Seq(season) => validateSeason(season)
      case _ => Flow.left[F, Seq[String]](InvalidArgsNumberException())
    }
  }

  def validateSeasonWithQtyArgs[F[_] : Monad](args: Seq[String]): Flow[F, Seq[String]] = {
    args match {
      case Seq() => Flow.right(args)
      case Seq(season) => validateSeason(season)
      case Seq(season, qty) => validateSeasonWithQty(season, qty)
      case _ => Flow.left[F, Seq[String]](InvalidArgsNumberException())
    }
  }

  def validateSeasonWithQty[F[_] : Monad](season: String, qty: String): Flow[F, Seq[String]] = {
    val res = for {
      s <- isSeasonValid(season)
      q <- isInt(qty)
    } yield Seq(s, q)
    Flow.fromResult(res)
  }

  def validateAddRoundArgs[F[_] : Monad](args: Seq[String]): Flow[F, Seq[String]] = {
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
}

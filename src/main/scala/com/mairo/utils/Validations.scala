package com.mairo.utils

import cats.Monad
import cats.syntax.either._
import com.mairo.exceptions.BotException.{WrongIntArgException, WrongSeasonArgException}
import com.mairo.utils.Flow.{Flow, Result}

import scala.util.{Failure, Success, Try}

trait Validations {

  def isSeasonValid(season: String): Result[String] = {
    val pattern = "^[Ss][1-4]\\|\\d{4}$".r
    pattern.findFirstMatchIn(season) match {
      case Some(_) => season.asRight[Throwable]
      case None => WrongSeasonArgException(season).asLeft[String]
    }
  }

  def isInt(str: String): Result[String] = {
    Try(str.toInt) match {
      case Failure(exception) => WrongIntArgException(str, exception).asLeft[String]
      case Success(_) => str.asRight[Throwable]
    }
  }

  def checkPlayerPairs(args: Seq[String]): Boolean = {
    val wOk = args.head.contains("/")
    val lOk = args.tail.head.contains("/")
    wOk && lOk
  }

  def validateSeason[F[_] : Monad](season: String): Flow[F, Seq[String]] = {
    Monad[F].map(Flow.fromResult(isSeasonValid(season)))(res => res.map(Seq(_)))
  }
}

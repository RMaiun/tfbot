package com.mairo.utils

import cats.implicits._
import com.mairo.exceptions.BotException.{WrongIntArgException, WrongSeasonArgException}
import com.mairo.utils.Flow.Result

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
}

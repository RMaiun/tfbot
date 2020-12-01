package com.mairo.services

import cats.implicits._
import cats.{ApplicativeError, MonadError}
import com.mairo.exceptions.BotException._

import scala.util.{Failure, Success, Try}

class ArgValidator[F[_]](implicit MT: MonadError[F, Throwable]) {

  def validateLinkTidArgs(args: Seq[String]): F[Seq[String]] = {
    args match {
      case Seq(_, _) => MT.pure(args)
      case _ => MT.raiseError(InvalidArgsNumberException())
    }
  }

  def validateAddPlayerArgs(args: Seq[String]): F[Seq[String]] = {
    if(args.size == 1){
      MT.pure(args)
    }else{
      MT.raiseError(InvalidArgsNumberException())
    }
  }

  def validateSeasonArgs(args: Seq[String]): F[Seq[String]] = {
    args match {
      case Seq() => MT.pure(args)
      case Seq(season) => validateSeason(season)
      case _ => MT.raiseError(InvalidArgsNumberException())
    }
  }

  def validateSeasonWithQtyArgs(args: Seq[String]): F[Seq[String]] = {
    args match {
      case Seq() => MT.pure(args)
      case Seq(season) => validateSeason(season)
      case Seq(season, qty) => validateSeasonWithQty(season, qty)
      case _ => MT.raiseError(InvalidArgsNumberException())
    }
  }

  def validateSeasonWithQty(season: String, qty: String): F[Seq[String]] = {
    for {
      s <- isSeasonValid(season)
      q <- isInt(qty)
    } yield Seq(s, q)
  }

  def validateAddRoundArgs(args: Seq[String]): F[Seq[String]] = {
    val trimArgs = args.map(_.trim)
    trimArgs.size match {
      case 2 =>
        if (checkPlayerPairs(trimArgs)) {
          MT.pure(trimArgs)
        } else {
          MT.raiseError(InvalidArgsNumberException())
        }
      case 3 =>
        if (checkPlayerPairs(trimArgs) && trimArgs.last.trim == "суха") {
          MT.pure(trimArgs)
        } else {
          MT.raiseError(WrongShutoutArgException(args.last))
        }
      case _ => MT.raiseError(WrongDefinedArgsNumberException(4, args.size))
    }
  }

  def isSeasonValid(season: String)(implicit AT: ApplicativeError[F, Throwable]): F[String] = {
    val pattern = "^[Ss][1-4]\\|\\d{4}$".r
    pattern.findFirstMatchIn(season) match {
      case Some(_) => AT.pure(season)
      case None => AT.raiseError(WrongSeasonArgException(season))
    }
  }

  def isInt(str: String)(implicit AT: ApplicativeError[F, Throwable]): F[String] = {
    Try(str.toInt) match {
      case Failure(exception) => AT.raiseError(WrongIntArgException(str, exception))
      case Success(_) => AT.pure(str)
    }
  }

  def checkPlayerPairs(args: Seq[String]): Boolean = {
    val wOk = args.head.contains("/")
    val lOk = args.tail.head.contains("/")
    wOk && lOk
  }

  def validateSeason(season: String)(implicit MT: MonadError[F, Throwable]): F[Seq[String]] = {
    for {
      res <- isSeasonValid(season)
    } yield Seq(res)
  }
}

package com.mairo.services

import cats.Monad
import com.mairo.utils.Flow
import com.mairo.utils.Flow.Flow

trait ArgValidator[F[_]] {
  def validate(args: Seq[String]): Flow[F, Seq[String]]
}

object ArgValidator {

  case class EmptyValidator[F[_] : Monad]() extends ArgValidator[F] {
    override def validate(args: Seq[String]): Flow[F, Seq[String]] = Flow.right(args)
  }

}
package com.mairo.utils

import cats.{Applicative, Monad}
import cats.implicits._

object Flow {
  type Result[T] = Either[Throwable,T]
  type Flow[F[_],T] = F[Result[T]]

  def right[F[_]:Applicative,T](data:T):Flow[F,T] = {
    Applicative[F].pure(data.asRight[Throwable])
  }
}

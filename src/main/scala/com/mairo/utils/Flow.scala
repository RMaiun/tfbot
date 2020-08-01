package com.mairo.utils

import cats.Applicative
import cats.implicits._

object Flow {
  type Result[T] = Either[Throwable, T]
  type Flow[F[_], T] = F[Result[T]]

  def right[F[_] : Applicative, T](data: T): Flow[F, T] = {
    Applicative[F].pure(data.asRight[Throwable])
  }
  def left[F[_] : Applicative,R ](data: Throwable): Flow[F, R] = {
    Applicative[F].pure(data.asLeft[R])
  }

  def fromResult[F[_] : Applicative, T](data: Result[T]): Flow[F, T] = {
    Applicative[F].pure(data)
  }
}

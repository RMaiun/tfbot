package com.mairo.utils

import cats.Monad
import cats.syntax.either._

object Flow {
  type Result[T] = Either[Throwable, T]
  type BinaryResult = Either[Throwable, Array[Byte]]
  type Flow[F[_], T] = F[Result[T]]
  type BinaryFlow[F[_]] = F[BinaryResult]

  def right[F[_] : Monad, T](data: T): Flow[F, T] = {
    Monad[F].pure(data.asRight[Throwable])
  }

  def left[F[_] : Monad, R](data: Throwable): Flow[F, R] = {
    Monad[F].pure(data.asLeft[R])
  }

  def fromResult[F[_] : Monad, T](data: Result[T]): Flow[F, T] = {
    Monad[F].pure(data)
  }

  def fromF[F[_] : Monad, T](data: F[T]): Flow[F, T] = {
    Monad[F].map(data)(_.asRight[Throwable])
  }
}

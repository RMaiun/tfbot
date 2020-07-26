package com.mairo.utils

object Aliases {
  type Result[T] = Either[Throwable,T]
  type Flow[F[_],T] = F[Result[T]]
}

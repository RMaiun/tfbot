package com.mairo.services

import cats.effect.Timer
import cats.implicits._
import cats.{Applicative, MonadError}
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.duration.FiniteDuration

object RetryService {

  def retry[F[_] : Applicative : Logger, A](f: => F[A], delays: Seq[FiniteDuration] = Seq())
                                           (implicit timer: Timer[F], monadThrowable: MonadError[F, Throwable]): F[A] = {
    retryInternally(f, delays)
  }

  def retryInternally[F[_] : Applicative : Logger, A](f: => F[A], delays: Seq[FiniteDuration] = Seq())
                                                     (implicit timer: Timer[F], monadThrowable: MonadError[F, Throwable]): F[A] = {
    f.recoverWith {
      case x: Throwable if delays.nonEmpty =>
        Logger[F].warn(s"Retry #${delays.size} request because of ${x.getClass.toString}: ${Option(x.getMessage).getOrElse("n/a")}")
          .flatMap(_ => timer.sleep(delays.head))
          .flatMap(_ => retry(f, delays.tail))
    }
  }
}

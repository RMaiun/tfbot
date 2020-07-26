package com.mairo.utils

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.sttp.{MonadAsyncError, MonadError, Request, Response, SttpBackend}

import scala.concurrent.{Future, Promise}

class IoSttpBackend(implicit cs: ContextShift[IO]) extends SttpBackend[IO, Nothing]{

  val c: SttpBackend[Future, Nothing] = OkHttpFutureBackend()
  override def send[T](request: Request[T, Nothing]): IO[Response[T]] = {
    IO.fromFuture(IO(c.send(request)))
  }

  override def close(): Unit = c.close()

  override def responseMonad: MonadError[IO] = new MonadAsyncError[IO]{
    override def async[T](register: (Either[Throwable, T] => Unit) => Unit): IO[T] = {
      val p = Promise[T]()
      register {
        case Left(t)  => p.failure(t)
        case Right(t) => p.success(t)
      }
      IO.fromFuture(IO(p.future))
    }

    override def unit[T](t: T): IO[T] = IO(t)

    override def map[T, T2](fa: IO[T])(f: T => T2): IO[T2] = fa.map(f)

    override def flatMap[T, T2](fa: IO[T])(f: T => IO[T2]): IO[T2] = fa.flatMap(f)

    override def error[T](t: Throwable): IO[T] = IO.raiseError(t)

    override protected def handleWrappedError[T](rt: IO[T])(h: PartialFunction[Throwable, IO[T]]): IO[T] = rt.recoverWith(h)
  }

}

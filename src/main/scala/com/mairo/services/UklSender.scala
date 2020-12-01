package com.mairo.services

import cats.MonadError
import cats.effect.Sync
import com.mairo.dtos.CataClientOutputDtos.{UklRequest, UklResponse}
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}

class UklSender[F[_] : Sync](rc: RabbitConfigurer[F])(implicit MT: MonadError[F, Throwable]) extends AppConfig with CataClientSprayCodecs {
  private val connection = rc.newConnection()
  private val channel = MT.flatMap(connection)(c => Sync[F].delay(c.createChannel()))

  def send(request: UklRequest): F[Unit] = {
    sendInternal(rabbitInputChannel, uklRequestFormat.write(request).toString().getBytes)
  }

  def selfSend(re: UklResponse): F[Unit] = {
    sendInternal(rabbitOutputChannel, uklResponseFormat.write(re).toString().getBytes)
  }

  private def sendInternal(destination: String, bytes: Array[Byte]): F[Unit] = {
    val sendEffect = MT.flatMap(channel)(c => Sync[F].delay(
      c.basicPublish("", destination, false, false,
        null, bytes)))
    MT.handleError(sendEffect)(err => Sync[F].delay(err.printStackTrace()))
  }
}

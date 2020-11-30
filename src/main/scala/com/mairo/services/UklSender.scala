package com.mairo.services

import cats.MonadError
import cats.effect.Sync
import com.mairo.dtos.CataClientOutputDtos.{UklRequest, UklResponse}
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}

class UklSender[F[_] : Sync](implicit MT: MonadError[F, Throwable]) extends AppConfig with CataClientSprayCodecs {
  private val connection = RabbitConfigurer.newConnection()
  private val channel = connection.createChannel()

  def send(request: UklRequest): F[Unit] = {
    val sendEffect = Sync[F].delay(
      channel.basicPublish("", rabbitInputChannel, false, false,
        null, uklRequestFormat.write(request).toString().getBytes)
    )
    MT.handleError(sendEffect)(err => Sync[F].delay(err.printStackTrace()))
  }

  def selfSend(re: UklResponse): F[Unit] = {
    val sendEffect = Sync[F].delay(
      channel.basicPublish("", rabbitOutputChannel, false, false,
        null, uklResponseFormat.write(re).toString().getBytes)
    )
    MT.handleError(sendEffect)(err => Sync[F].delay(err.printStackTrace()))
  }
}

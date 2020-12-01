package com.mairo.services

import java.time.Instant
import java.util.concurrent.Executors

import cats.{Applicative, MonadError}
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.syntax.flatMap._
import com.bot4s.telegram.models.{Chat, ChatType, Message}
import com.mairo.bot.CommandsBot
import com.mairo.dtos.CataClientOutputDtos.UklResponse
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}
import com.rabbitmq.client._
import io.chrisdavenport.log4cats.Logger
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import cats.syntax.flatMap._
class UklConsumer[F[_] : Applicative:Logger:Timer:ConcurrentEffect](bot: CommandsBot[F], rc:RabbitConfigurer[F])
                                                                   (implicit MT:MonadError[F,Throwable]) extends AppConfig with CataClientSprayCodecs {
  private val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  def startListener(): F[String] = {
    import cats.implicits._

    val effect = for{
      conn <- rc.newConnection(Some(ec))
      channel <- Sync[F].delay(conn.createChannel())
      consumerCode <- Sync[F].delay(channel.basicConsume(rabbitOutputChannel, consumer(channel)))
      _ <- Logger[F].info("Consumer was started successfully")
    }yield consumerCode
    RetryService.retry(effect, Seq(3 seconds, 3 seconds, 5 seconds, 10 seconds))

  }

  private def consumer(channel: Channel): Consumer = new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
      val str = new String(body)
      val result = str.parseJson.convertTo[UklResponse]
      val chatValue = Chat(result.chatId.toLong, ChatType.Private)
      val msg = Message(
        messageId = result.msgId,
        date = Instant.now().getEpochSecond.toInt,
        chat = chatValue
      )
      val effect = bot.replyWithMenu(result.result)(msg)
      val sendAck = ConcurrentEffect[F].pure(channel.basicAck(envelope.getDeliveryTag, false))

      ConcurrentEffect[F].toIO(effect >> sendAck).unsafeRunAsyncAndForget()
    }
  }
}

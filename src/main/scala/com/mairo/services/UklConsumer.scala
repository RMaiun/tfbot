package com.mairo.services

import java.time.Instant
import java.util.concurrent.Executors

import cats.effect.ConcurrentEffect
import cats.syntax.flatMap._
import com.bot4s.telegram.models.{Chat, ChatType, Message}
import com.mairo.bot.CommandsBot
import com.mairo.dtos.CataClientOutputDtos.UklResponse
import com.mairo.utils.{AppConfig, CataClientSprayCodecs}
import com.rabbitmq.client._
import spray.json._

import scala.concurrent.ExecutionContext

class UklConsumer[F[_] : ConcurrentEffect](bot: CommandsBot[F]) extends AppConfig with CataClientSprayCodecs {
  private val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())


  def startListener(): Unit = {
    val connection = RabbitConfigurer.newConnection()
    val channel = connection.createChannel()
    channel.basicConsume(rabbitOutputChannel, consumer(channel))
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

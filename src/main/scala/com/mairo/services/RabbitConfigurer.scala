package com.mairo.services

import java.util.concurrent.ExecutorService

import cats.MonadError
import cats.effect.{Sync, Timer}
import com.mairo.utils.AppConfig
import com.rabbitmq.client.{Connection, ConnectionFactory}
import io.chrisdavenport.log4cats.Logger

trait RabbitConfigurer[F[_]] {
  def newConnection(es: Option[ExecutorService] = None): F[Connection]
}

object RabbitConfigurer extends AppConfig {

  def apply[F[_]](implicit ev: RabbitConfigurer[F]): RabbitConfigurer[F] = ev

  def impl[F[_] : Sync : Timer : Logger](implicit MT: MonadError[F, Throwable]): RabbitConfigurer[F] = {
    new RabbitConfigurer[F] {
      val connectionFactory: F[ConnectionFactory] = factory()

      override def newConnection(es: Option[ExecutorService]): F[Connection] = {
        doConnection(es)
      }

      private def doConnection(es: Option[ExecutorService]): F[Connection] = {
        es match {
          case Some(value) =>
            MT.flatMap(connectionFactory)(cf => Sync[F].delay(cf.newConnection(value)))
          case None =>
            MT.flatMap(connectionFactory)(cf => Sync[F].delay(cf.newConnection()))
        }
      }

      private def factory(): F[ConnectionFactory] = {
        Sync[F].delay {
          val factory: ConnectionFactory = new ConnectionFactory
          factory.setAutomaticRecoveryEnabled(true)
          factory.setUsername(rabbitUser)
          factory.setPassword(rabbitPass)
          factory.setVirtualHost(rabbitVirtualHost)
          factory.setHost(rabbitHost)
          factory.setPort(rabbitPort)
          factory
        }
      }


    }
  }
}

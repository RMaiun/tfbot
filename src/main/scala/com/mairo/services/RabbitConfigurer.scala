package com.mairo.services

import java.util.concurrent.ExecutorService

import com.mairo.utils.AppConfig
import com.rabbitmq.client.{Connection, ConnectionFactory}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object RabbitConfigurer extends AppConfig {

  val connectionFactory: ConnectionFactory = factory()

  def newConnection(es: Option[ExecutorService] = None): Connection = {
    @tailrec
    def connection(retries: Int = 3): Connection = {
      if (retries == 0) {
        doConnection(es)
      } else {
        Try(doConnection(es)) match {
          case Failure(exception) =>
            println(exception.getMessage)
            println(s"Retry $retries executed")
            Thread.sleep(3000)
            connection(retries - 1)
          case Success(value) =>
            value
        }
      }
    }

    connection()
  }

  private def doConnection(es: Option[ExecutorService]): Connection = {
    es match {
      case Some(value) => connectionFactory.newConnection(value)
      case None => connectionFactory.newConnection()
    }
  }

  private def factory(): ConnectionFactory = {
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

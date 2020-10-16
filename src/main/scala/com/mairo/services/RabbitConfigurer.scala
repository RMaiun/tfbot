package com.mairo.services

import java.util.concurrent.Executors

import com.mairo.utils.AppConfig
import com.rabbitmq.client.{Connection, ConnectionFactory}

object RabbitConfigurer extends AppConfig{

  val connectionFactory: ConnectionFactory = factory()

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

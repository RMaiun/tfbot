package com.mairo.utils

import com.typesafe.config.ConfigFactory

trait AppConfig {

  case class RabbitConfig(user: String,
                          pass: String,
                          host: String,
                          virtualHost: String,
                          port: Int,
                          outputChannel: String
                         )

  private val config = ConfigFactory.load()

  private val botConfig = config.getConfig("bot")
  val botVersion: String = botConfig.getString("version")
  val botToken: String = botConfig.getString("token")
  val lastRoundsQty: Int = botConfig.getInt("lastRounds")

  private val cataclysmConfig = config.getConfig("cataclysm")
  val cataclysmHost: String = cataclysmConfig.getString("host")
  val cataclysmPort: String = cataclysmConfig.getString("port")
  val cataclysmRoot = s"http://$cataclysmHost:$cataclysmPort"

  private val rabbitConfig = config.getConfig("rabbit")
  val rabbitUser: String = rabbitConfig.getString("username")
  val rabbitPass: String = rabbitConfig.getString("password")
  val rabbitHost: String = rabbitConfig.getString("host")
  val rabbitVirtualHost: String = rabbitConfig.getString("virtualHost")
  val rabbitPort: Int = rabbitConfig.getInt("port")
  val rabbitOutputChannel: String = rabbitConfig.getString("outputChannel")
  val rabbitInputChannel: String = rabbitConfig.getString("inputChannel")
}

package com.mairo.utils

import com.typesafe.config.ConfigFactory

trait AppConfig {
  private val config = ConfigFactory.load()
  private val botConfig = config.getConfig("bot")
  val botVersion: String = botConfig.getString("version")
  val botToken: String = botConfig.getString("token")
  val lastRoundsQty:Int = botConfig.getInt("lastRounds")

}

package com.mairo.bot

import com.bot4s.telegram.models.Message
import com.mairo.ParentBot.notAvailable

trait SelfCommand {
  def selfCmdText(implicit msg: Message): String =
    s"""
       | *User info*:
       | messageId = ${msg.messageId}
       | chatId = ${msg.chat.id}
       | userId = ${msg.from.fold(notAvailable)(x => x.id.toString)}
       | firstName = ${msg.from.fold(notAvailable)(x => x.firstName)}
       | lastName = ${msg.from.flatMap(x => x.lastName).fold(notAvailable)(x => x)}
       | username = ${msg.from.flatMap(x => x.username).fold(notAvailable)(x => x)}""".stripMargin
}
package com.mairo.bot

import cats.Monad
import cats.effect.{Async, ContextShift}
import cats.implicits._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods.ParseMode
import com.bot4s.telegram.methods.ParseMode.ParseMode
import com.bot4s.telegram.models.{KeyboardButton, Message, ReplyKeyboardMarkup}
import com.mairo.bot.ParentBot._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import io.chrisdavenport.log4cats.Logger

object ParentBot {
  val START_CMD = "/start"
  val SELF_CMD = "/self"
  val PLAYERS_CMD = "/players"
  val LAST_CMD = "/last"
  val STATS_CMD = "/stats"
  val ADD_ROUND_CMD = "/add"
  val LOAD_XLSX_REPORT = "/xlsxReport"

  val notAvailable = "n/a"

}

abstract class ParentBot[F[_] : Async : ContextShift : Monad:Logger](val token: String)
  extends TelegramBot(token, AsyncHttpClientCatsBackend())
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  def defaultMarkup(): Option[ReplyKeyboardMarkup] = {
    val playersButton = KeyboardButton(PLAYERS_CMD)
    val statsButton = KeyboardButton(STATS_CMD)
    val lastButton = KeyboardButton(LAST_CMD)
    val xlsxButton = KeyboardButton(LOAD_XLSX_REPORT)

    val markup = ReplyKeyboardMarkup(Seq(Seq(statsButton, playersButton), Seq(lastButton, xlsxButton)),
      Some(true), Some(false), Some(true))
    Some(markup)
  }

  def response(result: String)(implicit msg: Message): F[Unit] = {
    replyWithMenu(result)
  }

  def replyWithMenu(text: String,
                    parseMode: Option[ParseMode] = None,
                    disableWebPagePreview: Option[Boolean] = None,
                    disableNotification: Option[Boolean] = None,
                    replyToMessageId: Option[Int] = None)(implicit message: Message): F[Unit] = {
    reply(text,
      parseMode = Some(ParseMode.Markdown),
      disableWebPagePreview,
      disableNotification,
      replyToMessageId,
      defaultMarkup()).void
  }

  def logCmdInvocation(cmd:String)(implicit msg:Message): F[Unit] ={
    Logger[F].info(s"$cmd was invoked by ${msg.from.fold("Incognito")(_.firstName)}")
  }
}
package com.mairo.bot

import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import cats.{Monad, MonadError}
import com.bot4s.telegram.methods.SendDocument
import com.bot4s.telegram.models.{InputFile, Message}
import com.mairo.bot.ParentBot._
import com.mairo.dtos.CataClientIntputDtos._
import com.mairo.dtos.CataClientOutputDtos.{UklRequest, UklResponse}
import com.mairo.services._
import com.mairo.utils.CataClientSprayCodecs
import io.chrisdavenport.log4cats.Logger

class CommandsBot[F[_] : Async : Timer : ContextShift : Monad : Logger](token: String,
                                                                        botVersion: String,
                                                                        argValidator: ArgValidator[F],
                                                                        uklSender: UklSender[F],
                                                                        cc: CataClient[F])
                                                                       (implicit MT: MonadError[F, Throwable])
  extends ParentBot[F](token)
    with StartCommand
    with SelfCommand
    with CataClientSprayCodecs {

  onCommand(START_CMD) { implicit msg =>
    for {
      _ <- logCmdInvocation(START_CMD)
      res <- response(startCmdText(botVersion))
    } yield res
  }

  onCommand(SELF_CMD) { implicit msg =>
    for {
      _ <- logCmdInvocation(SELF_CMD)
      res <- response(selfCmdText)
    } yield res
  }

  onCommand(PLAYERS_CMD) { implicit msg =>
    for {
      _ <- logCmdInvocation(PLAYERS_CMD)
      _ <- uklSender.send(UklRequest("listPlayers", msg.messageId, msg.chat.id.toString))
    } yield ()
  }

  onCommand(ADD_ROUND_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(ADD_ROUND_CMD)
        _ <- argValidator.validateAddRoundArgs(args)
        body <- MT.pure(addRoundFormat.write(prepareAddRoundDto(args, msg)))
        _ <- uklSender.send(UklRequest("addRound", msg.messageId, msg.chat.id.toString, Some(body)))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(LAST_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(LAST_CMD)
        _ <- argValidator.validateSeasonWithQtyArgs(args)
        body <- MT.pure(findLastRoundsFormat.write(prepareLastRoundArgs(args)))
        _ <- uklSender.send(UklRequest("findLastRounds", msg.messageId, msg.chat.id.toString, Some(body)))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(STATS_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(STATS_CMD)
        _ <- argValidator.validateSeasonArgs(args)
        body <- MT.pure(findShortStatsFormat.write(prepareFindShortStatsDto(args)))
        _ <- uklSender.send(UklRequest("shortStats", msg.messageId, msg.chat.id.toString, Some(body)))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(LINK_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(LINK_CMD)
        _ <- argValidator.validateLinkTidArgs(args)
        body <- MT.pure(linkTidFormat.write(LinkTidDto(args.head, args.tail.head, msg.from.fold("0")(_.id.toString))))
        _ <- uklSender.send(UklRequest("linkTid", msg.messageId, msg.chat.id.toString, Some(body)))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(SUBSCRIBE_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(SUBSCRIBE_CMD)
        body <- MT.pure(subscriptionActionFormat.write(SubscriptionActionDto(enableSubscriptions = true, msg.from.fold("0")(_.id.toString))))
        _ <- uklSender.send(UklRequest("subscribe", msg.messageId, msg.chat.id.toString, Some(body)))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(UNSUBSCRIBE_CMD) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(UNSUBSCRIBE_CMD)
        body <- MT.pure(subscriptionActionFormat.write(SubscriptionActionDto(enableSubscriptions = false, msg.from.fold("0")(_.id.toString))))
        _ <- uklSender.send(UklRequest("unsubscribe", msg.messageId, msg.chat.id.toString, Some(body)))
      } yield ()
      handleError(result, msg)
    }
  }

  onCommand(LOAD_XLSX_REPORT) { implicit msg =>
    withArgs { args =>
      val result = for {
        _ <- logCmdInvocation(LOAD_XLSX_REPORT)
        inputFile <- loadXlsxReport(args, msg)
        _ <- request(SendDocument(msg.chat.id, inputFile, replyMarkup = defaultMarkup()))
      } yield ()
      handleError(result, msg)
    }
  }

  private def handleError(fa: F[Unit], msg: Message): F[Unit] = {
    MT.handleErrorWith(fa)(err => uklSender.selfSend(UklResponse(msg.messageId, msg.chat.id.toString, err.getMessage)))
  }

  private def prepareAddRoundDto(args: Seq[String], msg: Message): AddRoundDto = {
    val winners = args.head.split("/")
    val losers = args.tail.head.split("/")
    AddRoundDto(winners(0), winners(1), losers(0), losers(1), args.size == 3, msg.from.fold("0")(_.id.toString))
  }

  private def prepareLastRoundArgs(args: Seq[String]): FindLastRounds = {
    args match {
      case Seq() => FindLastRounds(QuarterCalculator.currentQuarter)
      case Seq(s) => FindLastRounds(s)
      case Seq(s, q) => FindLastRounds(s, q.toInt)
    }
  }

  private def loadXlsxReport(args: Seq[String], msg: Message): F[InputFile] = {
    for {
      _ <- argValidator.validateSeasonArgs(args)
      season = if (args.isEmpty) QuarterCalculator.currentQuarter else args.head
      byteArr <- cc.getStatsXlsxDocument(season)
    } yield InputFile(s"$season-statistics.xlsx", byteArr)
  }

  private def prepareFindShortStatsDto(args: Seq[String]): FindShortStats = {
    args match {
      case Seq(s) => FindShortStats(s)
      case _ => FindShortStats(QuarterCalculator.currentQuarter)
    }
  }

}

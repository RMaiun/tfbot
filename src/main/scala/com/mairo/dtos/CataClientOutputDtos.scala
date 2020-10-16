package com.mairo.dtos

import java.time.LocalDateTime

import io.circe.Json
import spray.json.{JsObject, JsString, JsValue}

object CataClientOutputDtos {

  case class Player(id: Long, surname: String)

  case class Players(players: List[Player])

  case class PlayerStats(surname: String, score: BigDecimal)

  case class Streak(player: String, games: Int)

  case class ShortInfoStats(season: String,
                            playersRating: List[PlayerStats],
                            gamesPlayed: Int,
                            daysToSeasonEnd: Int,
                            bestStreak: Option[Streak],
                            worstStreak: Option[Streak])

  case class Round(winner1: String,
                   winner2: String,
                   loser1: String,
                   loser2: String,
                   created: LocalDateTime,
                   season: String,
                   shutout: Boolean)

  case class FoundLastRounds(rounds: List[Round])

  case class StoredId(id: Long)

  case class ExpectedException(reasons: List[String])

  case class Resource(name: String, content: Array[Byte])

  case class UklRequest(cmd:String, msgId:Int, chatId: String, data:JsValue = JsString(""))
  case class UklResponse(msgId:Int, chatId: String, result: String)

}

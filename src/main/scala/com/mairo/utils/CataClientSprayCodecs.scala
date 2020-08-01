package com.mairo.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.mairo.dtos.CataClientDtos._
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}

trait CataClientSprayCodecs extends DefaultJsonProtocol {
  implicit val localDateTimeFormat: JsonFormat[LocalDateTime] = new JsonFormat[LocalDateTime] {
    private val iso_date_time = DateTimeFormatter.ISO_DATE_TIME

    def write(x: LocalDateTime) = JsString(iso_date_time.format(x))

    def read(value: JsValue): LocalDateTime = value match {
      case JsString(x) => LocalDateTime.parse(x, iso_date_time)
      case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse LocalDateTime")
    }
  }

  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat2(Player)
  implicit val playersFormat: RootJsonFormat[Players] = jsonFormat1(Players)

  implicit val streakFormat: RootJsonFormat[Streak] = jsonFormat2(Streak)
  implicit val playerStatsFormat: RootJsonFormat[PlayerStats] = jsonFormat2(PlayerStats)
  implicit val shortInfoFormat: RootJsonFormat[ShortInfoStats] = jsonFormat6(ShortInfoStats)

  implicit val roundFormat: RootJsonFormat[Round] = jsonFormat7(Round)
  implicit val foundLastRoundsFormat: RootJsonFormat[FoundLastRounds] = jsonFormat1(FoundLastRounds)

}
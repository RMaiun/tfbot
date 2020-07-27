package com.mairo.utils

import com.mairo.dtos.CataClientDtos.{Player, Players}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait SprayCodecs extends DefaultJsonProtocol {
  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat2(Player)
  implicit val playersFormat: RootJsonFormat[Players] = jsonFormat1(Players)
}

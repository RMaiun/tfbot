package com.mairo.dtos

object CataClientDtos {

  case class Player(id: Long, surname: String)

  case class Players(players: List[Player])

}

package com.mairo.dtos

object CataClientIntputDtos {

  case class AddRoundDto(w1: String,
                         w2: String,
                         l1: String,
                         l2: String,
                         shutout: Boolean,
                         moderator: String)

  case class FindLastRounds(season: String, qty: Int = 6)

  case class FindShortStats(season:String)

  case class FetchLastRounds(season:String, qty: Int)

}

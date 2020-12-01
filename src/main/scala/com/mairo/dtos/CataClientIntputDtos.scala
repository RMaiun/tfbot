package com.mairo.dtos

object CataClientIntputDtos {

  case class AddRoundDto(w1: String,
                         w2: String,
                         l1: String,
                         l2: String,
                         shutout: Boolean,
                         moderator: String)

  case class AddPlayerDto(surname: String, moderator: String, admin: Boolean = false, tid: Option[String] = None)

  case class FindLastRounds(season: String, qty: Int = 6)

  case class FindShortStats(season: String)

  case class FetchLastRounds(season: String, qty: Int)

  case class LinkTidDto(tid: String, nameToLink: String, moderator: String)

  case class SubscriptionActionDto(enableSubscriptions: Boolean, tid: String)

}

package com.mairo.dtos

object CataClientIntputDtos {

  case class AddRoundDto(w1: String,
                         w2: String,
                         l1: String,
                         l2: String,
                         shutout: Boolean,
                         moderator: String)

}
package com.mairo.dtos

import java.time.LocalDateTime

object CataClientDtos {

  case class Player(id: Long, surname: String)

  case class Players(players: List[Player])

  case class PlayerStats(surname: String, score: Int)

  case class Streak(player: String, games: Int)

  case class ShortInfoStats(season: String,
                            playersRating: List[PlayerStats],
                            gamesPlayed: Int,
                            daysToSeasonEnd: Int,
                            bestStreak: Streak,
                            worstStreak: Streak)

  case class Round(winner1: String,
                   winner2: String,
                   loser1: String,
                   loser2: String,
                   created: LocalDateTime,
                   season: String,
                   shutout: Boolean)

  case class FoundLastRounds(rounds: List[Round])

}
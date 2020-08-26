package com.mairo.services

import cats.Monad
import com.mairo.dtos.CataClientOutputDtos._
import com.mairo.utils.Flow
import com.mairo.utils.Flow.Flow

object MessageFormatter {
  def formatPlayers[F[_] : Monad](data: Players): Flow[F, String] = {
    val players = data.players.map(x => s"${x.id}|${x.surname.capitalize}").mkString("\n")
    val result =
      s"""```
         |$players```
       """.stripMargin
    Flow.right(result)
  }

  def formatShortInfoStats[F[_] : Monad](data: ShortInfoStats): Flow[F, String] = {
    data.gamesPlayed match {
      case 0 =>
        val msg =
          s"""```
             |No games found in season ${data.season}```
           """.stripMargin
        Flow.right(msg)
      case _ =>
        val ratings = data.playersRating.indices.map(a => {
          val index = a + 1
          val name = data.playersRating(a).surname.capitalize
          val points = data.playersRating(a).score
          s"$index. $name $points"
        }).mkString(System.lineSeparator())
        val bestStreak = s"${data.bestStreak.fold("-")(_.player)}: ${data.bestStreak.fold("-")(_.games.toString)} games in row"
        val worstStreak = s"${data.worstStreak.fold("-")(_.player)}: ${data.worstStreak.fold("-")(_.games.toString)} games in row"
        val msg =
          s"""```
             |Season: ${data.season}
             |Games played: ${data.gamesPlayed}
             |Days till season end: ${data.daysToSeasonEnd}
             |${"-" * 30}
             |Current Rating:
             |$ratings
             |${"-" * 30}
             |Best Streak:
             |$bestStreak
             |${"-" * 30}
             |Worst Streak:
             |$worstStreak```
      """.stripMargin
        Flow.right(msg)
    }
  }

  def formatLastRounds[F[_] : Monad](data: FoundLastRounds): Flow[F, String] = {
    val result = data.rounds match {
      case Nil => "``` No data found```"
      case _ => formatLastRounds(data.rounds)
    }
    Flow.right(result)
  }

  private def formatLastRounds(rounds: List[Round]): String = {
    val line = "-" * 34
    val formedBlocks = rounds.map(r =>
      s"""
         |date: ${DateFormatter.formatDate(r.created)}
         |winners: ${r.winner1}/${r.winner2}
         |losers: ${r.loser1}/${r.loser2}${if (r.shutout) s"${System.lineSeparator()}|shutout: ✓" else ""}
         |""".stripMargin
    ).mkString(line)
    s"``` $formedBlocks```"
  }

  def formatStoredId[F[_] : Monad](data: StoredId): Flow[F, String] = {
    val msg =
      s"""```
         |New round was stored with id ${data.id}```
        """.stripMargin
    Flow.right(msg)
  }
}
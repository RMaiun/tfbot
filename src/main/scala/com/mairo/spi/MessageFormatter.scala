package com.mairo.spi

import cats.Monad
import com.mairo.dtos.CataClientDtos.{FoundLastRounds, Players, Round, ShortInfoStats}
import com.mairo.services.DateFormatter
import com.mairo.utils.Flow
import com.mairo.utils.Flow.Flow

trait MessageFormatter[F[_], A] {
  def format(data: A): Flow[F, String]
}

object MessageFormatter {

  case class PlayersCmdFormatter[F[_] : Monad]() extends MessageFormatter[F, Players] {
    override def format(data: Players): Flow[F, String] = {
      val players = data.players.map(x => s"${x.id}|${x.surname.capitalize}").mkString("\n")
      val result =
        s"""```
           |$players```
       """.stripMargin
      Flow.right(result)
    }
  }

  case class StatsCmdFormatter[F[_] : Monad]() extends MessageFormatter[F, ShortInfoStats] {
    override def format(data: ShortInfoStats): Flow[F, String] = {
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
  }

  case class LastCmdFormatter[F[_] : Monad]() extends MessageFormatter[F, FoundLastRounds] {
    override def format(data: FoundLastRounds): Flow[F, String] = {
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
  }

}

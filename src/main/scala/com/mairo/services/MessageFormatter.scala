package com.mairo.services

import cats.Monad
import com.mairo.dtos.CataClientDtos.Players
import com.mairo.utils.Flow
import com.mairo.utils.Flow.Flow

trait MessageFormatter[F[_], A] {
  def format(data: A): Flow[F, String]
}

object MessageFormatter {

  case class PlayersCmdFormatter[F[_] : Monad]() extends MessageFormatter[F, Players] {
    override def format(data: Players): Flow[F, String] =
      Flow.right(data.players.map(x => s"${x.id}|${x.surname.capitalize}")
        .mkString("\n"))
  }

}

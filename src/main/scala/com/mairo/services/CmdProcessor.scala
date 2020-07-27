package com.mairo.services

import cats.Monad
import cats.effect.ContextShift
import com.bot4s.telegram.models.Message
import com.mairo.dtos.CataClientDtos.Players
import com.mairo.utils.Flow.Flow

trait CmdProcessor[F[_], A] {
  def process(msg: Message, args: Seq[String] = Seq()): Flow[F, A]
}

object CmdProcessor {

  case class PlayersCmdProcessor[F[_] : Monad : ContextShift, A]()(cc: CataClient[F]) extends CmdProcessor[F, Players] {
    override def process(msg: Message, args: Seq[String]): Flow[F, Players] = cc.fetchPlayers()
  }

}

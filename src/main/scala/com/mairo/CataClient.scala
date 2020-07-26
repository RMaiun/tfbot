package com.mairo

import cats.{Applicative, Monad, MonadError}
import cats.effect.{Async, ContextShift, Sync}
import com.softwaremill.sttp._
import spray.json.DefaultJsonProtocol
import com.softwaremill.sttp.sprayJson._
import spray.json._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
case class Player(id: Long, surname: String)

case class Players(players: List[Player])

class CataClient[F[_] : ContextShift](implicit be: SttpBackend[F, Nothing], F:MonadError[F,Throwable])
  extends DefaultJsonProtocol
  with LazyLogging{

  //  implicit val successModelDecoder: Decoder[Player] = deriveDecoder[Player]
  //  implicit val errorModelDecoder: Decoder[Players] = deriveDecoder[Players]
  implicit val format = jsonFormat2(Player)
  implicit val format2 = jsonFormat1(Players)

  def fetchPlayers(): F[Either[String, Players]] = {
    logger.info("prepare request")
    val request = sttp.get(uri"http://localhost:8080/players/all")
        .response(asJson[Players])
    logger.info("send request")
    val response = be.send(request).map(resp => {
      logger.info(s"received response with status ${resp.code}")
      if(resp.isSuccess){
        resp.body
      }else{
        s"Failed to call cata [status:${resp.code}]".asLeft[Players]
      }
    })
    F.handleError(response)(err => {
      err.printStackTrace()
      err.getMessage.asLeft[Players]
    })
  }
}
package com.mairo.exceptions

class BotException(msg:String, cause:Option[Throwable] = None) extends RuntimeException(msg, cause.orNull)

object BotException {
  case class WrongSeasonArgException(value:String) extends BotException(s"Argument $value doesn't match season pattern (Example: S1|2020)")
  case class WrongIntArgException(value:String,cause:Throwable) extends BotException(s"Argument $value is not Integer", Some(cause))
  case class InvalidArgsNumberException() extends BotException("Wrong args quantity. Check /start for instructions")
}

package com.mairo.exceptions

class BotException(msg: String, cause: Option[Throwable] = None) extends RuntimeException(msg, cause.orNull)

object BotException {

  case class WrongSeasonArgException(value: String) extends BotException(s"Argument $value doesn't match season pattern (Example: S1|2020)")

  case class WrongShutoutArgException(value: String) extends BotException(s"Shutout is described with 'суха' constant but received $value ")

  case class WrongIntArgException(value: String, cause: Throwable) extends BotException(s"Argument $value is not Integer", Some(cause))

  case class InvalidArgsNumberException() extends BotException("Wrong args quantity. Check /start for instructions")

  case class WrongDefinedArgsNumberException(expected: Int, received: Int) extends BotException(s"Wrong args quantity: expected $expected but received $received")

  case class CataclysmUnexpectedException(cause: Throwable) extends BotException(s"Remote server exception", Some(cause))

  case class CataclysmExpectedException(msg: String) extends BotException(msg)

}

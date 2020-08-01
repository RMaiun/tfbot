package com.mairo.services

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

object DateFormatter {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  def formatDateWithHour(date: LocalDateTime): String = {
    val month = date.getMonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val dateTime = date.format(formatter)
    val year = date.getYear
    s"$dateTime $month $year"
  }

  def formatDate(date: LocalDateTime): String = {
    val day = date.getDayOfMonth
    val month = date.getMonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val hourAndMinutes = date.toLocalTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    s"$day $month $hourAndMinutes"
  }

}

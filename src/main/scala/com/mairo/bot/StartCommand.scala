package com.mairo.bot

import com.mairo.bot.ParentBot._

trait StartCommand {
  def startCmdText(version: String): String = {
    s"""
       |$START_CMD - інфа про можливості бота
       |--------------------------------------
       |$SELF_CMD - Дані про себе
       |--------------------------------------
       |$PLAYERS_CMD - Всі існуючі юзери з id
       |--------------------------------------
       |$LAST_CMD [s][n] - показати n останніх матчів
       |(s - опціонально, по дефолту current season)
       |(n - опціонально, по дефолту 6)
       |--------------------------------------
       |$STATS_CMD [x] - рейтинг гравців у сезоні
       |(x - формат сезона, опціонально)
       |якщо x відсутній, то now()
       |приклад: S1|2019, S4|2020
       |--------------------------------------
       |$SUBSCRIBE_CMD - увімкнути сповіщення
       |--------------------------------------
       |$UNSUBSCRIBE_CMD - вимкнути сповіщення
       |--------------------------------------
       |$LOAD_XLSX_REPORT_CMD [s] - згенерувати xlsx репорт для сезону
       |(s - опціонально, по дефолту - той, що на даний момент відкритий)
       |--------------------------------------
       |$ADD_ROUND_CMD [surname1/surname2 surname3/surname4] - додати гру
       |(тільки для адмінів)
       |--------------------------------------
       |$ADD_PLAYER_CMD [surname] - додати нового гравця
       |(тільки для адмінів)
       |--------------------------------------
       |$LINK_CMD [chatId surname] - увімкнути сповіщення юзеру
       |(тільки для адмінів)
       |--------------------------------------
       |$DUMP_CMD  - створити дамп
       |(тільки для адмінів)
       |--------------------------------------
       |           version:$version
                    """.stripMargin
  }
}

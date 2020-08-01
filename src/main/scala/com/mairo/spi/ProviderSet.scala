package com.mairo.spi

import com.mairo.dtos.CataClientDtos.{FoundLastRounds, Players, ShortInfoStats}

case class ProviderSet[F[_]](playersCmdSP: ServiceProvider[F, Players],
                             statsCmdSP: ServiceProvider[F, ShortInfoStats],
                             lastCmdSP: ServiceProvider[F, FoundLastRounds])

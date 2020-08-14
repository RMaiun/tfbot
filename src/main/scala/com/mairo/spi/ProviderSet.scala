package com.mairo.spi

import com.mairo.dtos.CataClientOutputDtos.{FoundLastRounds, Players, ShortInfoStats, StoredId}

case class ProviderSet[F[_]](playersCmdSP: ServiceProvider[F, Players],
                             statsCmdSP: ServiceProvider[F, ShortInfoStats],
                             lastCmdSP: ServiceProvider[F, FoundLastRounds],
                             addRoundCmdSP: ServiceProvider[F, StoredId],
                             loadXlsxReportCmdSP: BinaryServiceProvider[F])

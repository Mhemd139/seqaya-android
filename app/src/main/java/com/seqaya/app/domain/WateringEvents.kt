package com.seqaya.app.domain

import com.seqaya.app.domain.model.Reading
import java.time.Instant

fun wateringEvents(readings: List<Reading>): List<Instant> {
    if (readings.size < 2) return emptyList()
    return readings.zipWithNext()
        .filter { (prev, curr) -> !prev.isValveOpen && curr.isValveOpen }
        .map { (_, curr) -> curr.recordedAt }
}

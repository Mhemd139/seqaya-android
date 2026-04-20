package com.seqaya.app.ui.home

import java.time.Duration
import java.time.Instant

fun relativeTime(instant: Instant, now: Instant = Instant.now()): String {
    val minutes = Duration.between(instant, now).toMinutes().coerceAtLeast(0L)
    return when {
        minutes < 1L -> "just now"
        minutes < 60L -> "${minutes}m ago"
        minutes < 24L * 60L -> "${minutes / 60L}h ago"
        minutes < 30L * 24L * 60L -> "${minutes / (24L * 60L)}d ago"
        else -> "long ago"
    }
}

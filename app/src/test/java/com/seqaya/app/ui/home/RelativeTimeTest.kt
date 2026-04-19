package com.seqaya.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class RelativeTimeTest {
    private val now = Instant.parse("2026-04-19T12:00:00Z")

    @Test fun `less than a minute reads just now`() {
        val t = now.minus(Duration.ofSeconds(30))
        assertEquals("just now", relativeTime(t, now))
    }

    @Test fun `minutes read with m ago suffix`() {
        val t = now.minus(Duration.ofMinutes(17))
        assertEquals("17m ago", relativeTime(t, now))
    }

    @Test fun `hours read with h ago suffix`() {
        val t = now.minus(Duration.ofHours(3))
        assertEquals("3h ago", relativeTime(t, now))
    }

    @Test fun `days read with d ago suffix`() {
        val t = now.minus(Duration.ofDays(2))
        assertEquals("2d ago", relativeTime(t, now))
    }

    @Test fun `month old reads long ago`() {
        val t = now.minus(Duration.ofDays(90))
        assertEquals("long ago", relativeTime(t, now))
    }
}

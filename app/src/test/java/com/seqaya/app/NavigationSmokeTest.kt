package com.seqaya.app

import com.seqaya.app.ui.navigation.TopLevelDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationSmokeTest {
    @Test
    fun `bottom nav exposes three destinations in canonical order`() {
        val routes = TopLevelDestination.entries.map { it.route }
        assertEquals(listOf("home", "scan", "library"), routes)
    }
}

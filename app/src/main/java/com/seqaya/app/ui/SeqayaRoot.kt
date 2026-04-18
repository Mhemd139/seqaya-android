package com.seqaya.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seqaya.app.ui.components.PaperGrain
import com.seqaya.app.ui.home.HomePlaceholderScreen
import com.seqaya.app.ui.navigation.SeqayaBottomBar
import com.seqaya.app.ui.navigation.TopLevelDestination
import com.seqaya.app.ui.plants.LibraryPlaceholderScreen
import com.seqaya.app.ui.scan.ScanPlaceholderScreen
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun SeqayaRoot() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = TopLevelDestination.entries
        .firstOrNull { it.route == backStack?.destination?.route }
        ?: TopLevelDestination.Home

    Scaffold(
        containerColor = Seqaya.colors.bgCream,
        contentColor = Seqaya.colors.textPrimary,
        bottomBar = {
            SeqayaBottomBar(
                current = current,
                onSelect = { dest ->
                    if (dest.route != current.route) {
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PaperGrain(modifier = Modifier.fillMaxSize())
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.Home.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(TopLevelDestination.Home.route) { HomePlaceholderScreen() }
                composable(TopLevelDestination.Scan.route) { ScanPlaceholderScreen() }
                composable(TopLevelDestination.Library.route) { LibraryPlaceholderScreen() }
            }
        }
    }
}

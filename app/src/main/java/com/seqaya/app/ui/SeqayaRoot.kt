package com.seqaya.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seqaya.app.domain.model.AuthState
import com.seqaya.app.ui.components.PaperGrain
import com.seqaya.app.ui.device.DeviceDetailScreen
import com.seqaya.app.ui.home.HomeScreen
import com.seqaya.app.ui.navigation.SeqayaBottomBar
import com.seqaya.app.ui.contextual.ContextualActionScreen
import com.seqaya.app.ui.provisioning.AddDeviceScreen
import com.seqaya.app.ui.navigation.TopLevelDestination
import com.seqaya.app.ui.plants.LibraryPlaceholderScreen
import com.seqaya.app.ui.scan.ScanPlaceholderScreen
import com.seqaya.app.ui.settings.SettingsScreen
import com.seqaya.app.ui.signin.SignInScreen
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun SeqayaRoot(
    viewModel: AppRootViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()
    when (authState) {
        AuthState.Loading -> EdgeToEdgeSurface {}
        AuthState.Unauthenticated, is AuthState.Error -> EdgeToEdgeSurface {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                SignInScreen()
            }
        }
        is AuthState.Authenticated -> SignedInRoot()
    }
}

@Composable
private fun EdgeToEdgeSurface(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        PaperGrain(modifier = Modifier.fillMaxSize())
        content()
    }
}

@Composable
private fun SignedInRoot() {
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
                    if (dest.route == current.route) return@SeqayaBottomBar
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PaperGrain(modifier = Modifier.fillMaxSize())
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.Home.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(TopLevelDestination.Home.route) {
                    HomeScreen(
                        onDeviceClick = { serial -> navController.navigate("device/$serial") },
                        onAddDevice = { navController.navigate("addDevice") },
                        onSettingsClick = { navController.navigate("settings") },
                    )
                }
                composable("settings") {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(TopLevelDestination.Scan.route) { ScanPlaceholderScreen() }
                composable(TopLevelDestination.Library.route) { LibraryPlaceholderScreen() }
                composable(
                    route = "device/{serial}",
                    arguments = listOf(navArgument("serial") { type = NavType.StringType }),
                ) { entry ->
                    val serial = entry.arguments?.getString("serial").orEmpty()
                    DeviceDetailScreen(
                        onBack = { navController.popBackStack() },
                        onContextualAction = { action ->
                            if (serial.isNotEmpty()) {
                                navController.navigate("contextual/$action/$serial")
                            }
                        },
                    )
                }
                composable("addDevice") {
                    AddDeviceScreen(
                        onFinish = { serial, _ ->
                            navController.popBackStack()
                            navController.navigate("device/$serial") { launchSingleTop = true }
                        },
                        onCancel = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "contextual/{action}/{serial}",
                    arguments = listOf(
                        navArgument("action") { type = NavType.StringType },
                        navArgument("serial") { type = NavType.StringType },
                    ),
                ) {
                    ContextualActionScreen(
                        onDismiss = { navController.popBackStack() },
                        onChainToWetMapping = { serial ->
                            navController.popBackStack()
                            navController.navigate("contextual/WetMap/$serial")
                        },
                    )
                }
            }
        }
    }
}

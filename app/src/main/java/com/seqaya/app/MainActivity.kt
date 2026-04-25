package com.seqaya.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.os.Build
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.seqaya.app.ui.SeqayaRoot
import com.seqaya.app.ui.auth.SplashScreen
import com.seqaya.app.ui.theme.Seqaya
import com.seqaya.app.ui.theme.SeqayaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/**
 * Single activity. Hosts the system splash (Theme.Seqaya.Splash → animated
 * leaf on cream), then hands off to the in-app splash composable, which
 * choreographs into the rest of the app.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Hold the system splash only until Compose paints its first frame.
        // The launch moment is the icon-to-splash morph (handled by the
        // system) — we want to dismiss the splash as soon as the in-app
        // composable can take over, so the leaf the user sees IS the leaf
        // the morph just placed there.
        val splashScreen = installSplashScreen()
        var contentReady = false
        splashScreen.setKeepOnScreenCondition { !contentReady }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(onFirstFrame = { contentReady = true })
        }
    }
}

@Composable
private fun App(onFirstFrame: () -> Unit) {
    SeqayaTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Seqaya.colors.bgCream),
            color = Seqaya.colors.bgCream,
        ) {
            var splashFinished by remember { mutableStateOf(false) }
            if (splashFinished) {
                SeqayaRoot()
            } else {
                SplashScreen(
                    onFinished = { splashFinished = true },
                )
            }
            // Release the system splash gate exactly when the first frame of
            // the in-app composition is laid out — the in-app splash takes
            // over without a visible seam (subject to the minimum-duration
            // floor enforced by setKeepOnScreenCondition).
            LaunchedEffect(Unit) {
                // API 31+: system splash plays the 1000ms leaf_awaken AVD.
                // Release only after the animation completes so the user sees it.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) delay(1_000L)
                onFirstFrame()
            }
        }
    }
}

package com.seqaya.app.ui.components

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Best-effort reduce-motion signal. Compose's `LocalAccessibilityManager` does not
 * expose a dedicated reduced-motion flag, so we use touch-exploration (screen
 * reader enabled) as a proxy — the population most affected by continuous
 * motion is also the one most likely to have touch-exploration on.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        am?.isTouchExplorationEnabled == true
    }
}

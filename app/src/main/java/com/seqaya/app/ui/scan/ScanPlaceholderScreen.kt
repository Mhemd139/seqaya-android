package com.seqaya.app.ui.scan

import androidx.compose.runtime.Composable
import com.seqaya.app.ui.components.TabPlaceholder

@Composable
fun ScanPlaceholderScreen() {
    TabPlaceholder(
        eyebrow = "SCAN",
        title = "Identify a plant.",
        body = "Point the camera at a leaf — coming in phase 7.",
    )
}

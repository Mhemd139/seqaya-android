package com.seqaya.app.ui.home

import androidx.compose.runtime.Composable
import com.seqaya.app.ui.components.TabPlaceholder

@Composable
fun HomePlaceholderScreen() {
    TabPlaceholder(
        eyebrow = "HOME",
        title = "Your plants live here.",
        body = "Devices will appear once you add your first Seqaya.",
    )
}

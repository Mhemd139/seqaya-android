package com.seqaya.app.ui.plants

import androidx.compose.runtime.Composable
import com.seqaya.app.ui.components.TabPlaceholder

@Composable
fun LibraryPlaceholderScreen() {
    TabPlaceholder(
        eyebrow = "LIBRARY",
        title = "A small field guide.",
        body = "Browse plants — coming in phase 7.",
    )
}

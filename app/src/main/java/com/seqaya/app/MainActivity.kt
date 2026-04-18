package com.seqaya.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.seqaya.app.ui.SeqayaRoot
import com.seqaya.app.ui.theme.Seqaya
import com.seqaya.app.ui.theme.SeqayaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    SeqayaTheme {
        Surface(
            modifier = Modifier.fillMaxSize().background(Seqaya.colors.bgCream),
            color = Seqaya.colors.bgCream,
        ) {
            SeqayaRoot()
        }
    }
}

package com.seqaya.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.seqaya.app.ui.theme.Seqaya
import kotlinx.coroutines.delay

/**
 * The universal success moment. Used at the end of every NFC flow:
 *   - Leaf-spinner fades out (200 ms)
 *   - [illustration] composable slot fades in and scales 0.9 → 1.0 (400 ms, eased)
 *   - [headline] types in character-by-character (50 ms stagger)
 *   - Wood chime fires at t+100 ms via [onChime]
 *
 * Caller supplies the illustration slot (e.g. `OpeningLeafIllustration()`) and the
 * headline text. After the sequence, [onComplete] fires if not null so the caller
 * can navigate back.
 */
@Composable
fun SuccessCelebration(
    headline: String,
    modifier: Modifier = Modifier,
    illustration: @Composable () -> Unit,
    onChime: () -> Unit = {},
    onComplete: (() -> Unit)? = null,
    autoDismissMillis: Long = 2500L,
) {
    var showIllustration by remember { mutableStateOf(false) }
    var charsShown by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(200) // Let the spinner fade out first
        showIllustration = true
        delay(100)
        onChime()
        delay(300)
        // Type the headline in
        while (charsShown < headline.length) {
            charsShown++
            delay(50)
        }
        if (onComplete != null) {
            delay(autoDismissMillis)
            onComplete()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (showIllustration) 1f else 0.9f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "celebrationScale",
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            visible = showIllustration,
            enter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.scale(scale)) { illustration() }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = headline.take(charsShown),
            style = Seqaya.type.hL,
            color = Seqaya.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

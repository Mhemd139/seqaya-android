package com.seqaya.app.ui.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.seqaya.app.ui.theme.Seqaya

private const val MIN_TARGET = 10
private const val MAX_TARGET = 90
private const val DEFAULT_TARGET = 60

@Composable
fun EditTargetDialog(
    currentTarget: Int?,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var value by remember {
        mutableIntStateOf((currentTarget ?: DEFAULT_TARGET).coerceIn(MIN_TARGET, MAX_TARGET))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Target moisture") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { if (value > MIN_TARGET) value-- },
                    enabled = value > MIN_TARGET,
                    modifier = Modifier.semantics { contentDescription = "Decrease target" },
                ) {
                    Text(text = "\u2212", style = Seqaya.type.hL, color = Seqaya.colors.textPrimary)
                }
                Text(
                    text = "$value%",
                    style = Seqaya.type.hL,
                    color = Seqaya.colors.textPrimary,
                )
                IconButton(
                    onClick = { if (value < MAX_TARGET) value++ },
                    enabled = value < MAX_TARGET,
                    modifier = Modifier.semantics { contentDescription = "Increase target" },
                ) {
                    Text(text = "+", style = Seqaya.type.hL, color = Seqaya.colors.textPrimary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

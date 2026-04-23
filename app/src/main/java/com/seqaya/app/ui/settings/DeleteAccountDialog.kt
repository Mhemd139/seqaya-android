package com.seqaya.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.seqaya.app.R
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun DeleteAccountDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var step by remember { mutableStateOf(DeleteStep.Warning) }
    var typed by remember { mutableStateOf("") }
    val requiredWord = stringResource(R.string.delete_confirm_required_word)
    val dismiss = { if (!isDeleting) { step = DeleteStep.Warning; typed = ""; onDismiss() } }

    when (step) {
        DeleteStep.Warning -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(R.string.delete_confirm_step1_title)) },
            text = { Text(stringResource(R.string.delete_confirm_step1_body)) },
            confirmButton = {
                TextButton(onClick = { step = DeleteStep.Type }) {
                    Text(
                        text = stringResource(R.string.delete_confirm_step1_continue),
                        color = Seqaya.colors.accentBrown,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = dismiss) { Text(stringResource(R.string.cancel)) }
            },
        )
        DeleteStep.Type -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(R.string.delete_confirm_step2_title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.delete_confirm_step2_hint))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        singleLine = true,
                        enabled = !isDeleting,
                        label = { Text(stringResource(R.string.delete_confirm_step2_input_label)) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = !isDeleting && typed == requiredWord,
                ) {
                    Text(
                        text = if (isDeleting) stringResource(R.string.delete_in_progress)
                               else stringResource(R.string.delete_confirm_step2_action),
                        color = Seqaya.colors.accentBrown,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = dismiss, enabled = !isDeleting) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

private enum class DeleteStep { Warning, Type }

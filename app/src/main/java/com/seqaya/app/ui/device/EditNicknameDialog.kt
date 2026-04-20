package com.seqaya.app.ui.device

import androidx.compose.foundation.focusable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction

private const val MAX_NICKNAME_LENGTH = 30

@Composable
fun EditNicknameDialog(
    currentNickname: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draft by remember { mutableStateOf(currentNickname.orEmpty()) }
    val focusRequester = remember { FocusRequester() }
    val trimmed = draft.trim()
    val canSave = trimmed.isNotEmpty()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { if (it.length <= MAX_NICKNAME_LENGTH) draft = it },
                label = { Text("Nickname") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .focusable(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(trimmed) },
                enabled = canSave,
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

package com.seqaya.app.ui.provisioning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun WifiStep(
    ssid: String,
    password: String,
    ssidPrefilled: Boolean,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNext: () -> Unit,
    error: String?,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Which Wi-Fi?",
            style = Seqaya.type.hXL,
            color = Seqaya.colors.textPrimary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Your device connects to this network. 2.4 GHz only — most home networks are fine.",
            style = Seqaya.type.body.copy(fontSize = 14.sp),
            color = Seqaya.colors.textSecondary,
        )
        Spacer(Modifier.height(28.dp))

        FieldLabel("Network")
        Spacer(Modifier.height(6.dp))
        FieldBox {
            BasicTextField(
                value = ssid,
                onValueChange = onSsidChange,
                singleLine = true,
                textStyle = Seqaya.type.body.copy(color = Seqaya.colors.textPrimary),
                cursorBrush = SolidColor(Seqaya.colors.accentGreen),
                modifier = Modifier.weight(1f),
            )
            if (ssidPrefilled) {
                Text(
                    text = "PREFILLED",
                    style = Seqaya.type.labelCaps.copy(fontSize = 9.sp),
                    color = Seqaya.colors.textTertiary,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        FieldLabel("Password")
        Spacer(Modifier.height(6.dp))
        FieldBox {
            BasicTextField(
                value = password,
                onValueChange = onPasswordChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                textStyle = Seqaya.type.body.copy(color = Seqaya.colors.textPrimary),
                cursorBrush = SolidColor(Seqaya.colors.accentGreen),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (passwordVisible) "hide" else "show",
                style = Seqaya.type.caption,
                color = Seqaya.colors.textSecondary,
                modifier = Modifier
                    .clickable { passwordVisible = !passwordVisible }
                    .semantics { contentDescription = "Toggle password visibility" }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "We never store your password.",
            style = Seqaya.type.fine,
            color = Seqaya.colors.textTertiary,
        )

        if (error != null) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Seqaya.colors.accentBrownSoft)
                    .border(1.dp, Seqaya.colors.accentBrown, RoundedCornerShape(10.dp))
                    .padding(12.dp),
            ) {
                Text(text = error, style = Seqaya.type.body, color = Seqaya.colors.accentBrownInk)
            }
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onNext,
            enabled = ssid.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Seqaya.colors.accentGreen,
                contentColor = Seqaya.colors.bgCream,
                disabledContainerColor = Seqaya.colors.borderStrong,
                disabledContentColor = Seqaya.colors.textTertiary,
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("Next", style = Seqaya.type.body)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = Seqaya.type.labelCaps,
        color = Seqaya.colors.textSecondary,
    )
}

@Composable
private fun FieldBox(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Seqaya.colors.bgCreamLightest)
            .border(1.dp, Seqaya.colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        content = content,
    )
}

@Suppress("unused")
private fun unused(c: Canvas) {} // keep Canvas import used when UI grows

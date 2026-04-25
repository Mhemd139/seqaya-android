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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.ui.theme.Seqaya
import com.seqaya.app.wifi.WifiNetwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiStep(
    ssid: String,
    password: String,
    ssidPrefilled: Boolean,
    locationServicesOff: Boolean,
    pickerOpen: Boolean,
    pickerNetworks: List<WifiNetwork>,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onPickNetwork: (String) -> Unit,
    onOpenLocationSettings: () -> Unit,
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
            text = "Your device connects to this network. 2.4 GHz only — most home networks are fine.",
            style = Seqaya.type.body.copy(fontSize = 14.sp),
            color = Seqaya.colors.textSecondary,
        )
        Spacer(Modifier.height(28.dp))

        if (locationServicesOff) {
            LocationServicesBanner(onOpenSettings = onOpenLocationSettings)
            Spacer(Modifier.height(20.dp))
        }

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
        Spacer(Modifier.height(8.dp))
        PickFromNearbyButton(onClick = onOpenPicker)

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

    if (pickerOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onClosePicker,
            sheetState = sheetState,
            containerColor = Seqaya.colors.bgCream,
            dragHandle = null,
        ) {
            NetworkPickerSheet(
                networks = pickerNetworks,
                selectedSsid = ssid,
                onPick = onPickNetwork,
            )
        }
    }
}

/**
 * Banner shown when the OS Location toggle is off. Without it, every Wi-Fi
 * read returns redacted data (`<unknown ssid>`) regardless of permission grants —
 * Android's privacy contract, not something we can read around.
 */
@Composable
private fun LocationServicesBanner(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Seqaya.colors.accentBrownSoft)
            .border(1.dp, Seqaya.colors.accentBrown, RoundedCornerShape(10.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "Turn on Location to detect your Wi-Fi",
            style = Seqaya.type.body.copy(fontWeight = FontWeight.SemiBold),
            color = Seqaya.colors.accentBrownInk,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Android requires the system Location toggle to be on so apps can read the Wi-Fi name. We don't track your location.",
            style = Seqaya.type.body.copy(fontSize = 13.sp),
            color = Seqaya.colors.accentBrownInk,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Open Location settings",
            style = Seqaya.type.caption.copy(fontWeight = FontWeight.Medium),
            color = Seqaya.colors.accentBrownInk,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenSettings)
                .semantics { contentDescription = "Open system Location settings" }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun PickFromNearbyButton(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Pick from nearby Wi-Fi networks" }
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "Pick from nearby networks",
            style = Seqaya.type.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = Seqaya.colors.accentGreen,
        )
    }
}

@Composable
private fun NetworkPickerSheet(
    networks: List<WifiNetwork>,
    selectedSsid: String,
    onPick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = "Nearby networks",
            style = Seqaya.type.hXL.copy(fontSize = 22.sp),
            color = Seqaya.colors.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap to use. 5 GHz networks won't work — your Seqaya needs 2.4 GHz.",
            style = Seqaya.type.body.copy(fontSize = 13.sp),
            color = Seqaya.colors.textSecondary,
        )
        Spacer(Modifier.height(14.dp))

        if (networks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No networks found. Type the name manually.",
                    style = Seqaya.type.body.copy(fontSize = 14.sp),
                    color = Seqaya.colors.textTertiary,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(items = networks, key = { it.ssid }) { network ->
                    NetworkRow(
                        network = network,
                        isSelected = network.ssid == selectedSsid,
                        onClick = if (network.is24GHz) {
                            { onPick(network.ssid) }
                        } else null,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun NetworkRow(
    network: WifiNetwork,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
) {
    val isDisabled = onClick == null
    val rowModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(vertical = 12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier,
    ) {
        SignalBars(
            bars = network.signalBars,
            tint = if (isDisabled) Seqaya.colors.textTertiary else Seqaya.colors.textPrimary,
        )
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = network.ssid,
                style = Seqaya.type.body.copy(
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = if (isDisabled) Seqaya.colors.textTertiary else Seqaya.colors.textPrimary,
            )
            if (isSelected) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Connected",
                    style = Seqaya.type.fine.copy(fontSize = 11.sp),
                    color = Seqaya.colors.accentGreen,
                )
            }
        }
        Spacer(Modifier.size(10.dp))
        BandTag(is24GHz = network.is24GHz)
    }
}

@Composable
private fun SignalBars(bars: Int, tint: Color) {
    val clamped = bars.coerceIn(0, 3)
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.size(width = 18.dp, height = 14.dp),
    ) {
        for (i in 0..3) {
            val barHeight = (4 + i * 3).dp
            val isActive = i <= clamped
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = barHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) tint else tint.copy(alpha = 0.25f)),
            )
        }
    }
}

@Composable
private fun BandTag(is24GHz: Boolean) {
    val (label, dotColor, textColor, bg) = if (is24GHz) {
        BandTagStyle("2.4G", Seqaya.colors.accentGreen, Seqaya.colors.accentGreenInk, Seqaya.colors.accentGreenSoft)
    } else {
        BandTagStyle("5G", Seqaya.colors.textTertiary, Seqaya.colors.textTertiary, Seqaya.colors.bgCreamLightest)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.size(5.dp))
        Text(
            text = label,
            style = Seqaya.type.labelCaps.copy(fontSize = 10.sp),
            color = textColor,
        )
    }
}

private data class BandTagStyle(
    val label: String,
    val dotColor: Color,
    val textColor: Color,
    val bg: Color,
)

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

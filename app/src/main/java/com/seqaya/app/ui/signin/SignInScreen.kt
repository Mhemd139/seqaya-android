package com.seqaya.app.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seqaya.app.R
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Seqaya.colors.bgCream)
            .padding(horizontal = 28.dp)
            .padding(top = 18.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = Seqaya.type.wordmark.copy(
                color = Seqaya.colors.textPrimary,
                fontSize = Seqaya.type.wordmark.fontSize * 0.83f,
            ),
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            val titleStyle = Seqaya.type.hXL.copy(color = Seqaya.colors.textPrimary, fontSize = 42.sp)
            Text(text = stringResource(R.string.sign_in_title_line_1), style = titleStyle)
            Text(text = stringResource(R.string.sign_in_title_line_2), style = titleStyle)

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.sign_in_lede),
                style = Seqaya.type.bodySecondary.copy(color = Seqaya.colors.textSecondary),
            )

            Spacer(Modifier.height(32.dp))

            GoogleButton(
                inFlight = state.inFlight,
                onClick = { viewModel.signIn(context) },
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.sign_in_legal),
                style = Seqaya.type.fine.copy(color = Seqaya.colors.textTertiary),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            val error = state.errorMessage
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Seqaya.colors.accentBrownSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = error,
                        style = Seqaya.type.bodySecondary.copy(color = Seqaya.colors.accentBrownInk),
                    )
                    Text(
                        text = stringResource(R.string.sign_in_error_dismiss),
                        style = Seqaya.type.caption.copy(color = Seqaya.colors.accentBrownInk),
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickable(onClick = viewModel::dismissError),
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.sign_in_footer),
            style = Seqaya.type.italic.copy(
                color = Seqaya.colors.textTertiary,
                fontSize = 13.sp,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun GoogleButton(inFlight: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val googleGlyphDescription = stringResource(R.string.google_g_content_description)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Seqaya.colors.accentGreen)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !inFlight,
                onClick = onClick,
            )
            .semantics { role = Role.Button },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (inFlight) {
            CircularProgressIndicator(
                color = Seqaya.colors.bgCream,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .semantics { contentDescription = googleGlyphDescription },
            ) {
                GoogleGlyph(modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.size(10.dp))
            Text(
                text = stringResource(R.string.sign_in_button),
                style = Seqaya.type.body.copy(
                    color = Seqaya.colors.bgCream,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

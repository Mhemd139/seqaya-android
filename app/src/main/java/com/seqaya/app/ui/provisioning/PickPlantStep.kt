package com.seqaya.app.ui.provisioning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.domain.model.Plant
import com.seqaya.app.ui.components.LeafSpinner
import com.seqaya.app.ui.components.OpeningLeafIllustration
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun PickPlantStep(
    plants: List<Plant>,
    loading: Boolean,
    selected: Plant?,
    onSelect: (Plant) -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "What are you growing?",
            style = Seqaya.type.hXL,
            color = Seqaya.colors.textPrimary,
        )
        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LeafSpinner(size = 56.dp)
                }
                plants.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No plants loaded. Check your connection and try again.",
                        style = Seqaya.type.body,
                        color = Seqaya.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(plants, key = { it.id }) { plant ->
                        PlantCard(
                            plant = plant,
                            selected = plant.id == selected?.id,
                            onClick = { onSelect(plant) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onNext,
            enabled = selected != null,
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
private fun PlantCard(
    plant: Plant,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Seqaya.colors.accentBrown else Seqaya.colors.border
    val bgColor: Color =
        if (selected) Seqaya.colors.accentBrownSoft else Seqaya.colors.bgCreamLightest

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = "${plant.commonName ?: plant.scientificName} plant option"
            }
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.height(72.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            OpeningLeafIllustration(size = 60.dp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = plant.commonName ?: plant.scientificName,
            style = Seqaya.type.hM.copy(fontSize = 14.5.sp),
            color = Seqaya.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = plant.scientificName,
            style = Seqaya.type.italic.copy(fontSize = 11.5.sp),
            color = Seqaya.colors.textTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

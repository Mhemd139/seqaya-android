package com.seqaya.app.data.remote.dto

import com.seqaya.app.domain.model.Plant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlantDto(
    val id: String,
    @SerialName("scientific_name") val scientificName: String,
    @SerialName("common_name") val commonName: String? = null,
    @SerialName("moisture_target") val moistureTarget: Int? = null,
    @SerialName("illustration_key") val illustrationKey: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
) {
    fun toDomain(): Plant = Plant(
        id = id,
        scientificName = scientificName,
        commonName = commonName,
        moistureTargetPercent = moistureTarget,
        illustrationKey = illustrationKey ?: "generic",
        imageUrl = imageUrl,
    )
}

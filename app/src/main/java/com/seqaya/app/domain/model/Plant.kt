package com.seqaya.app.domain.model

/**
 * A plant entry from the shared Supabase `plants` table.
 *
 * In Phase 5 this is used to populate the Add Device wizard's plant picker (B·04).
 * The [id] is the UUID row key; [moistureTargetPercent] is the recommended default
 * moisture threshold (from PlantBook), and [illustrationKey] maps to one of the
 * hand-drawn illustrations the design bundle ships with (fig, monstera, pothos,
 * snake, basil, succulent).
 */
data class Plant(
    val id: String,
    val scientificName: String,
    val commonName: String?,
    val moistureTargetPercent: Int?,
    val illustrationKey: String = "generic",
    val imageUrl: String? = null,
)

/** The 6 defaults that ship with the app when the Supabase `plants` table is empty. */
val DefaultPlants: List<Plant> = listOf(
    Plant(
        id = "default-peace-lily",
        scientificName = "Spathiphyllum wallisii",
        commonName = "Peace Lily",
        moistureTargetPercent = 57,
        illustrationKey = "generic",
    ),
    Plant(
        id = "default-fig",
        scientificName = "Ficus lyrata",
        commonName = "Fiddle Leaf Fig",
        moistureTargetPercent = 55,
        illustrationKey = "fig",
    ),
    Plant(
        id = "default-monstera",
        scientificName = "Monstera deliciosa",
        commonName = "Monstera",
        moistureTargetPercent = 55,
        illustrationKey = "monstera",
    ),
    Plant(
        id = "default-pothos",
        scientificName = "Epipremnum aureum",
        commonName = "Pothos",
        moistureTargetPercent = 45,
        illustrationKey = "pothos",
    ),
    Plant(
        id = "default-snake",
        scientificName = "Sansevieria trifasciata",
        commonName = "Snake plant",
        moistureTargetPercent = 30,
        illustrationKey = "snake",
    ),
    Plant(
        id = "default-basil",
        scientificName = "Ocimum basilicum",
        commonName = "Basil",
        moistureTargetPercent = 65,
        illustrationKey = "basil",
    ),
    Plant(
        id = "default-succulent",
        scientificName = "Echeveria",
        commonName = "Succulent",
        moistureTargetPercent = 25,
        illustrationKey = "succulent",
    ),
)

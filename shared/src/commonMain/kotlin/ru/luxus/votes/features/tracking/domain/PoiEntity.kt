package ru.luxus.votes.features.tracking.domain

data class PoiEntity(
    val id: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

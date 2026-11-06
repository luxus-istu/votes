package ru.luxus.votes.features.tracking.domain.repository

import ru.luxus.votes.features.tracking.domain.PoiEntity

interface TrackingRepository {

    fun getPois(): List<PoiEntity>
}
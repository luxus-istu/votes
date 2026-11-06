package ru.luxus.votes.features.lessons.data.entity

import androidx.compose.runtime.Immutable
import ru.luxus.votes.features.lessons.domain.model.UdmurtLetterModel

@Immutable
data class UdmurtLetterEntity(
    val upper: String,
    val lower: String,
    val transcription: String,
) {
    fun toModel() = UdmurtLetterModel(
        upper,
        lower,
        transcription,
    )
}

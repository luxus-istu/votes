package ru.luxus.votes.features.lessons.domain.model

import androidx.compose.runtime.Immutable
import ru.luxus.votes.features.lessons.data.entity.UdmurtLetterEntity

@Immutable
data class UdmurtLetterModel(
    val upper: String,
    val lower: String,
    val transcription: String,
) {
    fun toEntity() = UdmurtLetterEntity(
        upper,
        lower,
        transcription,
    );
}
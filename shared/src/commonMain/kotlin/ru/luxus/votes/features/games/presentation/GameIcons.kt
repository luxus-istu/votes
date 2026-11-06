package ru.luxus.votes.features.games.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One icon language for the game hub and the parent dashboard.
 * Word emojis are separate: they are picture hints on vocabulary cards.
 */
fun gameIcon(gameKey: String): ImageVector = when (gameKey) {
    "translate" -> Icons.Filled.Translate
    "truefalse" -> Icons.AutoMirrored.Filled.FactCheck
    "oddoneout" -> Icons.Filled.Search
    "sorting" -> Icons.Filled.Category
    "memory" -> Icons.Filled.Style
    "compile" -> Icons.Filled.Abc
    else -> Icons.Filled.Search
}
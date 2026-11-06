package ru.luxus.votes.core.clipboard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

@OptIn(ExperimentalComposeUiApi::class)
actual suspend fun Clipboard.copyText(text: String) {
    setClipEntry(ClipEntry.withPlainText(text))
}

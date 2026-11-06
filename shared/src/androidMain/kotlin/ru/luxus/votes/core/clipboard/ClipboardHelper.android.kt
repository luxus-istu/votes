package ru.luxus.votes.core.clipboard

import android.content.ClipData
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry

actual suspend fun Clipboard.copyText(text: String) {
    setClipEntry(ClipData.newPlainText("Текст", text).toClipEntry())
}

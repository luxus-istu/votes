package ru.luxus.votes.core.clipboard

import androidx.compose.ui.platform.Clipboard

/**
 * Copies plain [text] to the system clipboard.
 *
 * [androidx.compose.ui.platform.ClipEntry] has no common constructor, so the
 * platform-specific entry has to be built separately for Android and iOS.
 */
expect suspend fun Clipboard.copyText(text: String)

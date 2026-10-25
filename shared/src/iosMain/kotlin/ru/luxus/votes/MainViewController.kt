package ru.luxus.votes

import androidx.compose.ui.window.ComposeUIViewController
import ru.luxus.votes.core.di.initKoin
import ru.luxus.votes.core.sound.SoundPlayer
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        initKoin(SoundPlayer())
        koinStarted = true
    }
    return ComposeUIViewController { App() }
}

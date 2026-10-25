package ru.luxus.votes

import android.app.Application
import ru.luxus.votes.core.database.AndroidAppContext
import ru.luxus.votes.core.di.initKoin
import ru.luxus.votes.core.sound.SoundPlayer

class VotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidAppContext.value = this
        initKoin(SoundPlayer())
    }
}

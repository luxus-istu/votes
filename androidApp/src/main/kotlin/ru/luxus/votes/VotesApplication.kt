package ru.luxus.votes

import android.app.Application
import org.koin.android.ext.koin.androidContext
import ru.luxus.votes.db.DriverFactory
import ru.luxus.votes.di.initKoin

class VotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val driverFactory = DriverFactory(this)
        initKoin(driverFactory) {
            androidContext(this@VotesApplication)
        }
    }
}

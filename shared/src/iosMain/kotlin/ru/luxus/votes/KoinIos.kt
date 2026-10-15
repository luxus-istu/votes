package ru.luxus.votes

import ru.luxus.votes.db.DriverFactory
import ru.luxus.votes.di.initKoin

fun initKoinIos() {
    val driverFactory = DriverFactory()
    initKoin(driverFactory)
}

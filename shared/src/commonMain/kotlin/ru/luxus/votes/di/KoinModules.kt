package ru.luxus.votes.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.ksp.generated.*
import ru.luxus.votes.db.DriverFactory
import ru.luxus.votes.db.VotesDatabase

@Module(includes = [DatabaseModule::class])
@ComponentScan("ru.luxus.votes")
class AppModule

@Module
class DatabaseModule {

    @Single
    fun database(driverFactory: DriverFactory): VotesDatabase = VotesDatabase(driverFactory.createDriver())
}

fun initKoin(driverFactory: DriverFactory, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            AppModule().module,
            module {
                single { driverFactory }
            }
        )
    }
}

package ru.luxus.votes.core.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.plugin.module.dsl.modules
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.buildWithDriver
import ru.luxus.votes.core.database.createDatabaseBuilder
import ru.luxus.votes.core.sound.SoundPlayer

@Module
@ComponentScan("ru.luxus.votes")
class BootstrapModule

fun initKoin(
    soundPlayer: SoundPlayer,
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(BootstrapModule::class)
        modules(
            module {
                single<AppDatabase> { createDatabaseBuilder().buildWithDriver() }
                single { soundPlayer }
            },
        )
    }
}

package ru.luxus.votes.features.tracking.data.repository

import org.koin.core.annotation.Singleton
import ru.luxus.votes.features.tracking.domain.PoiEntity
import ru.luxus.votes.features.tracking.domain.repository.TrackingRepository

@Singleton(binds = [TrackingRepository::class])
class TrackingRepositoryImpl : TrackingRepository {
    private val pois = listOf(
        PoiEntity(
            id = "1",
            title = "Музей М.Т. Калашникова",
            description = "Оружейный музей, посвящённый жизни и разработкам М.Т. Калашникова.",
            latitude = 56.850703,
            longitude = 53.206610
        ),
        PoiEntity(
            id = "2",
            title = "Лудорвай",
            description = "Архитектурно-этнографический музей-заповедник под открытым небом.",
            latitude = 56.746786,
            longitude = 53.059506
        ),
        PoiEntity(
            id = "3",
            title = "Свято-Михайловский собор",
            description = "Православный храм на холме в Ижевске.",
            latitude = 56.849601,
            longitude = 53.205359
        ),
        PoiEntity(
            id = "4",
            title = "Зоопарк Удмуртии",
            description = "Зоопарк в Удмуртии.",
            latitude = 56.865537,
            longitude = 53.174118
        ),
        PoiEntity(
            id = "5",
            title = "Дача Башенина (Сарапул)",
            description = "Купеческая усадьба начала XX века в Прикамье.",
            latitude = 56.472718,
            longitude = 53.820927
        ),
        PoiEntity(
            id = "6",
            title = "Музей-усадьба П.И. Чайковского (Воткинск)",
            description = "Место рождения композитора на берегу пруда.",
            latitude = 56.328957,
            longitude = 36.747547
        )
    )

    override fun getPois(): List<PoiEntity> = pois
}

package ru.luxus.votes.features.tracking.data

import org.koin.core.annotation.Single
import ru.luxus.votes.features.tracking.domain.PoiEntity

@Single
class TrackingRepository {
    private val pois = listOf(
        PoiEntity(
            id = "1",
            title = "Музей М.Т. Калашникова",
            description = "Оружейный музей, посвященный жизни и разработкам М.Т. Калашникова.",
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
            description = "Величественный православный храм на холме в Ижевске.",
            latitude = 56.849601,
            longitude = 53.205359
        ),
        PoiEntity(
            id = "4",
            title = "Зоопарк Удмуртии",
            description = "Один из крупнейших и лучших зоологических парков России.",
            latitude = 56.865537,
            longitude = 53.174118
        ),
        PoiEntity(
            id = "5",
            title = "Дача Башенина (Сарапул)",
            description = "Архитектурная жемчужина Прикамья, купеческая усадьба начала XX века.",
            latitude = 56.472718,
            longitude = 53.820927
        ),
        PoiEntity(
            id = "6",
            title = "Музей-усадьба П.И. Чайковского (Воткинск)",
            description = "Место рождения великого композитора на берегу живописного пруда.",
            latitude = 56.328957,
            longitude = 36.747547
        )
    )

    fun getPois(): List<PoiEntity> = pois
}

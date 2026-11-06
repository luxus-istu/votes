package ru.luxus.votes.features.lessons.data.local

import org.koin.core.annotation.Singleton
import ru.luxus.votes.features.lessons.data.entity.UdmurtLetterEntity

private val SPECIAL_LETTERS = setOf("Ӝ", "Ӟ", "Ӥ", "Ӧ", "Ӵ")

private val UDMURT_ALPHABET = listOf(
    UdmurtLetterEntity("А", "а", "[а]"), UdmurtLetterEntity("Б", "б", "[бэ]"),
    UdmurtLetterEntity("В", "в", "[вэ]"), UdmurtLetterEntity("Г", "г", "[гэ]"),
    UdmurtLetterEntity("Д", "д", "[дэ]"), UdmurtLetterEntity("Е", "е", "[е]"),
    UdmurtLetterEntity("Ё", "ё", "[ё]"), UdmurtLetterEntity("Ж", "ж", "[жэ]"),
    UdmurtLetterEntity("Ӝ", "ӝ", "[джэ]"), UdmurtLetterEntity("З", "з", "[зэ]"),
    UdmurtLetterEntity("Ӟ", "ӟ", "[дзэ]"), UdmurtLetterEntity("И", "и", "[и]"),
    UdmurtLetterEntity("Ӥ", "ӥ", "[и]"), UdmurtLetterEntity("Й", "й", "[и краткое]"),
    UdmurtLetterEntity("К", "к", "[ка]"), UdmurtLetterEntity("Л", "л", "[эль]"),
    UdmurtLetterEntity("М", "м", "[эм]"), UdmurtLetterEntity("Н", "н", "[эн]"),
    UdmurtLetterEntity("О", "о", "[о]"), UdmurtLetterEntity("Ӧ", "ӧ", "[о]"),
    UdmurtLetterEntity("П", "п", "[пэ]"), UdmurtLetterEntity("Р", "р", "[эр]"),
    UdmurtLetterEntity("С", "с", "[эс]"), UdmurtLetterEntity("Т", "т", "[тэ]"),
    UdmurtLetterEntity("У", "у", "[у]"), UdmurtLetterEntity("Ф", "ф", "[эф]"),
    UdmurtLetterEntity("Х", "х", "[ха]"), UdmurtLetterEntity("Ц", "ц", "[цэ]"),
    UdmurtLetterEntity("Ч", "ч", "[че]"), UdmurtLetterEntity("Ӵ", "ӵ", "[чэ]"),
    UdmurtLetterEntity("Ш", "ш", "[ша]"), UdmurtLetterEntity("Щ", "щ", "[ща]"),
    UdmurtLetterEntity("Ъ", "ъ", "[твёрдый знак]"), UdmurtLetterEntity("Ы", "ы", "[ы]"),
    UdmurtLetterEntity("Ь", "ь", "[мягкий знак]"), UdmurtLetterEntity("Э", "э", "[э]"),
    UdmurtLetterEntity("Ю", "ю", "[ю]"), UdmurtLetterEntity("Я", "я", "[я]"),
)


@Singleton(binds = [WordDataSource::class])
class WordDataSourceImpl : WordDataSource {
    override suspend fun getAllLetters(): List<UdmurtLetterEntity> = UDMURT_ALPHABET;
    override suspend fun getAllSpecialLetters(): Set<String> = SPECIAL_LETTERS;
}
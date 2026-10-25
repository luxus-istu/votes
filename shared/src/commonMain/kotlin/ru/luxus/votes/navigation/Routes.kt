package ru.luxus.votes.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object HomeRoute

@Serializable
data object GamesHubRoute

// Mini-Game 1: Choose Translation (Выбери перевод)
@Serializable
data object ChooseTranslationRoute

// Mini-Game 2: Find the Odd One Out (Найди лишнее слово)
@Serializable
data object OddOneOutRoute

// Mini-Game 3: Sorting (Сортировка)
@Serializable
data object SortingGameRoute

// Mini-Game 4: Find the Pair (Найди пару / Memory)
@Serializable
data object MemoryGameRoute

// Mini-Game 5: Put Word Together (Собери слово)
@Serializable
data object CompileWordRoute

// Mini-Game 6: True / False (Правда / Ложь)
@Serializable
data object TrueFalseRoute

@Serializable
data object GameResultRoute

@Serializable
data object LessonsTopicsRoute

@Serializable
data class LessonCardsRoute(val topic: String)

@Serializable
data object ProfileRoute

@Serializable
data object EditProfileRoute

@Serializable
data object LanguageInfoRoute

@Serializable
data object ParentReportRoute

@Serializable
data object ParentDashboardRoute

@Serializable
data class ChildFormRoute(val childId: String? = null)

@Serializable
data object StatisticsRoute

@Serializable
data object SettingsRoute


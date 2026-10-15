package ru.luxus.votes.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object LoginRoute

@Serializable
data object HomeRoute

@Serializable
data object GamesHubRoute

@Serializable
data object ChooseTranslationRoute

@Serializable
data object CompileWordRoute

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
data object SettingsRoute

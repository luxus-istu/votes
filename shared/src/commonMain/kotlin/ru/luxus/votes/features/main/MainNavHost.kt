package ru.luxus.votes.features.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.luxus.votes.features.auth.presentation.AuthEvent
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.games.presentation.CompileWordPage
import ru.luxus.votes.features.games.presentation.ChooseTranslationPage
import ru.luxus.votes.features.games.presentation.GameResultPage
import ru.luxus.votes.features.games.presentation.GamesEvent
import ru.luxus.votes.features.games.presentation.GamesHubPage
import ru.luxus.votes.features.games.presentation.GamesViewModel
import ru.luxus.votes.features.games.presentation.MemoryGamePage
import ru.luxus.votes.features.games.presentation.OddOneOutPage
import ru.luxus.votes.features.games.presentation.SortingGamePage
import ru.luxus.votes.features.games.presentation.StatisticsPage
import ru.luxus.votes.features.games.presentation.TrueFalsePage
import ru.luxus.votes.features.home.presentation.HomePage
import ru.luxus.votes.features.lessons.presentation.AlphabetPage
import ru.luxus.votes.features.lessons.presentation.LessonCardsPage
import ru.luxus.votes.features.lessons.presentation.LessonsTopicsPage
import ru.luxus.votes.features.lessons.presentation.LessonsViewModel
import ru.luxus.votes.features.parent.presentation.ChildFormPage
import ru.luxus.votes.features.parent.presentation.ParentDashboardPage
import ru.luxus.votes.features.parent.presentation.ParentViewModel
import ru.luxus.votes.features.profile.presentation.EditProfilePage
import ru.luxus.votes.features.profile.presentation.LanguageInfoPage
import ru.luxus.votes.features.profile.presentation.ParentReportPage
import ru.luxus.votes.features.profile.presentation.ProfilePage
import ru.luxus.votes.features.settings.presentation.SettingsPage
import ru.luxus.votes.features.settings.presentation.SettingsViewModel
import ru.luxus.votes.navigation.AlphabetRoute
import ru.luxus.votes.navigation.ChooseTranslationRoute
import ru.luxus.votes.navigation.ChildFormRoute
import ru.luxus.votes.navigation.CompileWordRoute
import ru.luxus.votes.navigation.EditProfileRoute
import ru.luxus.votes.navigation.GameResultRoute
import ru.luxus.votes.navigation.GamesHubRoute
import ru.luxus.votes.navigation.HomeRoute
import ru.luxus.votes.navigation.LanguageInfoRoute
import ru.luxus.votes.navigation.LessonCardsRoute
import ru.luxus.votes.navigation.LessonsTopicsRoute
import ru.luxus.votes.navigation.MemoryGameRoute
import ru.luxus.votes.navigation.OddOneOutRoute
import ru.luxus.votes.navigation.ParentDashboardRoute
import ru.luxus.votes.navigation.ParentReportRoute
import ru.luxus.votes.navigation.ProfileRoute
import ru.luxus.votes.navigation.SettingsRoute
import ru.luxus.votes.navigation.SortingGameRoute
import ru.luxus.votes.navigation.StatisticsRoute
import ru.luxus.votes.navigation.TrueFalseRoute

private const val TRANSITION_DURATION = 200

/** Main app navigation, shown once a session (parent or child) is active. */
@Composable
fun MainNavHost(
    isParent: Boolean,
    authViewModel: AuthViewModel,
    gamesViewModel: GamesViewModel,
    lessonsViewModel: LessonsViewModel,
    settingsViewModel: SettingsViewModel,
    parentViewModel: ParentViewModel,
) {
    val navController = rememberNavController()

    // Parents live in their dashboard (no kids' menu); children start at home
    val startDestination: Any = if (isParent) ParentDashboardRoute else HomeRoute

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                targetOffset = { fullWidth -> fullWidth / 3 }
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                initialOffset = { fullWidth -> fullWidth / 3 }
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable<HomeRoute> {
            HomePage(
                authViewModel = authViewModel,
                gamesViewModel = gamesViewModel,
                onNavigateToGames = { navController.navigate(GamesHubRoute) },
                onNavigateToLessons = { navController.navigate(LessonsTopicsRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
            )
        }

        composable<GamesHubRoute> {
            GamesHubPage(
                viewModel = gamesViewModel,
                onNavigateToGame = { gameKey -> navController.navigateToGame(gameKey) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mini-Game 1: Choose Translation
        composable<ChooseTranslationRoute> {
            ChooseTranslationPage(
                viewModel = gamesViewModel,
                onGameComplete = { navController.navigate(GameResultRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mini-Game 2: Find the Odd One Out
        composable<OddOneOutRoute> {
            OddOneOutPage(
                viewModel = gamesViewModel,
                onGameComplete = { navController.navigate(GameResultRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mini-Game 3: Sorting
        composable<SortingGameRoute> {
            SortingGamePage(
                viewModel = gamesViewModel,
                onGameComplete = { navController.navigate(GameResultRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mini-Game 4: Memory (Find the Pair)
        composable<MemoryGameRoute> {
            MemoryGamePage(
                viewModel = gamesViewModel,
                onGameComplete = { navController.navigate(GameResultRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mini-Game 5: Compile Word
        composable<CompileWordRoute> {
            CompileWordPage(
                viewModel = gamesViewModel,
                onGameComplete = { navController.navigate(GameResultRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Mini-Game 6: True / False
        composable<TrueFalseRoute> {
            TrueFalsePage(
                viewModel = gamesViewModel,
                onGameComplete = { navController.navigate(GameResultRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Game Result
        composable<GameResultRoute> {
            GameResultPage(
                viewModel = gamesViewModel,
                onBackToGames = {
                    navController.popBackStack(GamesHubRoute, inclusive = false)
                }
            )
        }

        // Lessons Topics
        composable<LessonsTopicsRoute> {
            LessonsTopicsPage(
                viewModel = lessonsViewModel,
                onTopicSelected = { topic ->
                    navController.navigate(LessonCardsRoute(topic))
                },
                onOpenAlphabet = { navController.navigate(AlphabetRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        // Udmurt alphabet
        composable<AlphabetRoute> {
            AlphabetPage(
                viewModel = lessonsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Lesson Flashcards
        composable<LessonCardsRoute> { backStackEntry ->
            val route: LessonCardsRoute = backStackEntry.toRoute()
            LessonCardsPage(
                topic = route.topic,
                viewModel = lessonsViewModel,
                onBack = { navController.popBackStack() },
                onPlayRandomGame = { gameKey ->
                    // Start the game before navigating, otherwise the game screen
                    // has no questions to render (black screen)
                    gamesViewModel.onEvent(GamesEvent.StartGame(gameKey))
                    navController.navigateToGame(gameKey)
                }
            )
        }

        // Parent Dashboard (Кабинет родителя) – the only screen available to a parent account
        composable<ParentDashboardRoute> {
            ParentDashboardPage(
                viewModel = parentViewModel,
                onLogout = { authViewModel.onEvent(AuthEvent.Logout) },
                onAddChild = { navController.navigate(ChildFormRoute()) },
                onEditChild = { childId -> navController.navigate(ChildFormRoute(childId)) }
            )
        }

        // Child profile form (create / edit)
        composable<ChildFormRoute> { backStackEntry ->
            val route: ChildFormRoute = backStackEntry.toRoute()
            ChildFormPage(
                childId = route.childId,
                viewModel = parentViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Profile
        composable<ProfileRoute> {
            ProfilePage(
                authViewModel = authViewModel,
                gamesViewModel = gamesViewModel,
                onNavigateToEditProfile = { navController.navigate(EditProfileRoute) },
                onNavigateToLanguageInfo = { navController.navigate(LanguageInfoRoute) },
                onNavigateToParentReport = { navController.navigate(ParentReportRoute) },
                onNavigateToStatistics = { navController.navigate(StatisticsRoute) },
                onLoggedOut = { },
                onBack = { navController.popBackStack() }
            )
        }

        // Edit Profile
        composable<EditProfileRoute> {
            EditProfilePage(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Language Info
        composable<LanguageInfoRoute> {
            LanguageInfoPage(
                onBack = { navController.popBackStack() }
            )
        }

        // Parent / Teacher Report
        composable<ParentReportRoute> {
            ParentReportPage(
                authViewModel = authViewModel,
                gamesViewModel = gamesViewModel,
                lessonsViewModel = lessonsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Detailed Statistics
        composable<StatisticsRoute> {
            StatisticsPage(
                viewModel = gamesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable<SettingsRoute> {
            SettingsPage(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/** Opens the mini-game matching [gameKey] (also used by the lessons victory slide). */
private fun NavHostController.navigateToGame(gameKey: String) {
    when (gameKey) {
        "translate" -> navigate(ChooseTranslationRoute)
        "oddoneout" -> navigate(OddOneOutRoute)
        "sorting" -> navigate(SortingGameRoute)
        "memory" -> navigate(MemoryGameRoute)
        "compile" -> navigate(CompileWordRoute)
        "truefalse" -> navigate(TrueFalseRoute)
        else -> navigate(ChooseTranslationRoute)
    }
}

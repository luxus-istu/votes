package ru.luxus.votes

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import ru.luxus.votes.core.theme.T2Theme
import ru.luxus.votes.features.auth.presentation.AuthEvent
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.auth.presentation.LoginPage
import ru.luxus.votes.features.auth.presentation.SplashPage
import ru.luxus.votes.features.games.presentation.*
import ru.luxus.votes.features.home.presentation.HomePage
import ru.luxus.votes.features.lessons.presentation.LessonCardsPage
import ru.luxus.votes.features.lessons.presentation.LessonsTopicsPage
import ru.luxus.votes.features.lessons.presentation.LessonsViewModel
import ru.luxus.votes.features.profile.presentation.ProfilePage
import ru.luxus.votes.features.settings.presentation.SettingsPage
import ru.luxus.votes.features.settings.presentation.SettingsViewModel
import ru.luxus.votes.navigation.*

private const val TRANSITION_DURATION = 200

@Composable
fun App() {
    T2Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            val authViewModel: AuthViewModel = koinViewModel()
            val gamesViewModel: GamesViewModel = koinViewModel()
            val lessonsViewModel: LessonsViewModel = koinViewModel()
            val settingsViewModel: SettingsViewModel = koinViewModel()

            NavHost(
                navController = navController,
                startDestination = SplashRoute,
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
                composable<SplashRoute>(
                    enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
                    exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
                ) {
                    SplashPage(
                        onSplashFinished = {
                            authViewModel.onEvent(AuthEvent.CheckSession)
                            navController.navigate(LoginRoute) {
                                popUpTo(SplashRoute) { inclusive = true }
                            }
                        }
                    )
                }

                composable<LoginRoute>(
                    enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
                    exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
                ) {
                    LoginPage(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(HomeRoute) {
                                popUpTo(LoginRoute) { inclusive = true }
                            }
                        }
                    )
                }

                composable<HomeRoute> {
                    HomePage(
                        authViewModel = authViewModel,
                        onNavigateToGames = { navController.navigate(GamesHubRoute) },
                        onNavigateToLessons = { navController.navigate(LessonsTopicsRoute) },
                        onNavigateToProfile = { navController.navigate(ProfileRoute) },
                        onNavigateToSettings = { navController.navigate(SettingsRoute) }
                    )
                }

                composable<GamesHubRoute> {
                    GamesHubPage(
                        viewModel = gamesViewModel,
                        onNavigateToGame = { gameKey ->
                            when (gameKey) {
                                "compile" -> navController.navigate(CompileWordRoute)
                                "truefalse" -> navController.navigate(TrueFalseRoute)
                                else -> navController.navigate(ChooseTranslationRoute)
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<ChooseTranslationRoute> {
                    ChooseTranslationPage(
                        viewModel = gamesViewModel,
                        onGameComplete = { navController.navigate(GameResultRoute) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<CompileWordRoute> {
                    CompileWordPage(
                        viewModel = gamesViewModel,
                        onGameComplete = { navController.navigate(GameResultRoute) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<TrueFalseRoute> {
                    TrueFalsePage(
                        viewModel = gamesViewModel,
                        onGameComplete = { navController.navigate(GameResultRoute) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<GameResultRoute> {
                    GameResultPage(
                        viewModel = gamesViewModel,
                        onBackToGames = {
                            navController.popBackStack(GamesHubRoute, inclusive = false)
                        }
                    )
                }

                composable<LessonsTopicsRoute> {
                    LessonsTopicsPage(
                        viewModel = lessonsViewModel,
                        onTopicSelected = { topic ->
                            navController.navigate(LessonCardsRoute(topic))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<LessonCardsRoute> { backStackEntry ->
                    val route: LessonCardsRoute = backStackEntry.toRoute()
                    LessonCardsPage(
                        topic = route.topic,
                        viewModel = lessonsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<ProfileRoute> {
                    ProfilePage(
                        authViewModel = authViewModel,
                        gamesViewModel = gamesViewModel,
                        onLoggedOut = {
                            navController.navigate(LoginRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<SettingsRoute> {
                    SettingsPage(
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

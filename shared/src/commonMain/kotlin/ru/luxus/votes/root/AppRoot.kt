package ru.luxus.votes.root

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ru.luxus.votes.features.auth.presentation.AuthNavHost
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.games.presentation.GamesViewModel
import ru.luxus.votes.features.lessons.presentation.LessonsViewModel
import ru.luxus.votes.features.main.MainNavHost
import ru.luxus.votes.features.onboarding.presentation.OnboardingKind
import ru.luxus.votes.features.onboarding.presentation.OnboardingPage
import ru.luxus.votes.features.parent.presentation.ParentViewModel
import ru.luxus.votes.features.settings.presentation.SettingsViewModel

@Composable
fun AppRoot(
    viewModel: RootViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    gamesViewModel: GamesViewModel = koinViewModel(),
    lessonsViewModel: LessonsViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    parentViewModel: ParentViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Crossfade(targetState = state, label = "root") { rootState ->
            when (rootState) {
                RootState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                RootState.AppOnboarding -> OnboardingPage(
                    kind = OnboardingKind.APP,
                    onFinished = viewModel::onAppOnboardingFinished,
                )

                RootState.Unauthorized -> AuthNavHost(authViewModel)

                is RootState.Onboarding -> OnboardingPage(
                    kind = if (rootState.isParent) OnboardingKind.PARENT else OnboardingKind.CHILD,
                    onFinished = viewModel::onAccountOnboardingFinished,
                )

                is RootState.Authorized -> MainNavHost(
                    isParent = rootState.isParent,
                    authViewModel = authViewModel,
                    gamesViewModel = gamesViewModel,
                    lessonsViewModel = lessonsViewModel,
                    settingsViewModel = settingsViewModel,
                    parentViewModel = parentViewModel,
                )
            }
        }
    }
}

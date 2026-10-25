package ru.luxus.votes.features.auth.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.luxus.votes.navigation.LoginRoute

/** Navigation shown while no session is active (no splash screen – goes straight to login). */
@Composable
fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            LoginPage(
                viewModel = authViewModel,
                // Login success restores the session at the root, which swaps to MainNavHost
                onLoginSuccess = { }
            )
        }
    }
}

package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.Magenta
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.auth.presentation.AuthEvent
import ru.luxus.votes.features.auth.presentation.AuthState
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.games.presentation.GamesViewModel

@Composable
fun ProfilePage(
    authViewModel: AuthViewModel,
    gamesViewModel: GamesViewModel,
    onLoggedOut: () -> Unit,
    onBack: () -> Unit
) {
    val authState by authViewModel.state.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    val gamesState by gamesViewModel.state.collectAsState()
    val stats = gamesRepositoryStats(gamesViewModel)

    QuizScaffold(
        title = "Профиль",
        onBack = onBack
    ) {
        if (user != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Magenta)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(White.copy(alpha = 0.2f), RoundedCornerShape(100.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.firstName.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${user.firstName} ${user.lastName}".trim().ifEmpty { "Пользователь" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Статистика обучения",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ответов", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${stats.totalAnswered}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Верно", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${stats.totalCorrect}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Точность", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${(stats.accuracy * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            T2Button(
                text = "Выйти из аккаунта",
                onClick = {
                    authViewModel.onEvent(AuthEvent.Logout)
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = IncorrectRed
            )
        }
    }
}

@Composable
private fun gamesRepositoryStats(viewModel: GamesViewModel): ru.luxus.votes.features.games.domain.GameStats {
    val state by viewModel.state.collectAsState()
    return (state as? ru.luxus.votes.features.games.presentation.GamesState.Initial)?.stats
        ?: ru.luxus.votes.features.games.domain.GameStats()
}

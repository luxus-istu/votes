package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.auth.presentation.AuthEvent
import ru.luxus.votes.features.auth.presentation.AuthState
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.presentation.GamesState
import ru.luxus.votes.features.games.presentation.GamesViewModel

@Composable
fun ProfilePage(
    authViewModel: AuthViewModel,
    gamesViewModel: GamesViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLanguageInfo: () -> Unit,
    onNavigateToParentReport: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onLoggedOut: () -> Unit,
    onBack: () -> Unit
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val user = (authState as? AuthState.Authenticated)?.user

    val gamesState by gamesViewModel.state.collectAsStateWithLifecycle()
    val stats = (gamesState as? GamesState.Initial)?.stats ?: GameStats()

    QuizScaffold(
        title = "Профиль",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (user != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.avatar,
                                fontSize = 38.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${user.firstName} ${user.lastName}".trim().ifEmpty { "Ученик" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${user.role} • ${user.age} лет",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Статистика игр",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(
                                label = "Ответов",
                                value = "${stats.totalAnswered}",
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = "Верно",
                                value = "${stats.totalCorrect}",
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = "Точность",
                                value = "${(stats.accuracy * 100).toInt()}%",
                                accent = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Разделы",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileNavigationItem(
                    title = "Об удмуртском языке",
                    subtitle = "Алфавит и факты о языке",
                    icon = Icons.Default.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToLanguageInfo
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileNavigationItem(
                    title = "Отчёт для родителей и учителей",
                    subtitle = "Слова, точность и результаты игр",
                    icon = Icons.Default.School,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToParentReport
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileNavigationItem(
                    title = "Статистика",
                    subtitle = "Слова с ошибками и занятия по дням",
                    icon = Icons.Default.Assessment,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToStatistics
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileNavigationItem(
                    title = "Изменить профиль",
                    subtitle = "Имя, фамилия и аватар",
                    icon = Icons.Default.Edit,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToEditProfile
                )

                Spacer(modifier = Modifier.height(28.dp))

                T2Button(
                    text = "Выйти",
                    onClick = {
                        authViewModel.onEvent(AuthEvent.Logout)
                        onLoggedOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}

@Composable
private fun ProfileNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconTint.copy(alpha = 0.15f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
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
    val authState by authViewModel.state.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    val gamesState by gamesViewModel.state.collectAsState()
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
                // User Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = UdmurtRed)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(White.copy(alpha = 0.2f), CircleShape),
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )

                        Text(
                            text = "${user.role} • ${user.age} лет",
                            fontSize = 14.sp,
                            color = White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = user.email,
                            fontSize = 12.sp,
                            color = White.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Snapshot
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Игровая статистика",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                                Text("${(stats.accuracy * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UdmurtRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Разделы кабинета:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileNavigationItem(
                    title = "Об удмуртском языке",
                    subtitle = "Интересные факты, алфавит, количество носителей",
                    icon = Icons.Default.Info,
                    iconTint = UdmurtRed,
                    onClick = onNavigateToLanguageInfo
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileNavigationItem(
                    title = "Отчёт для родителей и учителей",
                    subtitle = "Сводка успехов, рекомендации и результаты ребёнка",
                    icon = Icons.Default.School,
                    iconTint = UdmurtGold,
                    onClick = onNavigateToParentReport
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileNavigationItem(
                    title = "Подробная статистика",
                    subtitle = "Сложные слова для повторения и прогресс по дням",
                    icon = Icons.Default.Assessment,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToStatistics
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileNavigationItem(
                    title = "Редактировать профиль",
                    subtitle = "Имя, возраст, роль и аватар-талисман",
                    icon = Icons.Default.Edit,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToEditProfile
                )

                Spacer(modifier = Modifier.height(28.dp))

                T2Button(
                    text = "Выйти из аккаунта",
                    onClick = {
                        authViewModel.onEvent(AuthEvent.Logout)
                        onLoggedOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = IncorrectRed
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 16.sp
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

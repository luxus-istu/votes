package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.auth.domain.UserEntity
import ru.luxus.votes.features.auth.presentation.AuthState
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.games.presentation.GamesState
import ru.luxus.votes.features.games.presentation.GamesViewModel
import ru.luxus.votes.features.lessons.presentation.LessonsState
import ru.luxus.votes.features.lessons.presentation.LessonsViewModel

@Composable
fun ParentReportPage(
    authViewModel: AuthViewModel,
    gamesViewModel: GamesViewModel,
    lessonsViewModel: LessonsViewModel,
    onBack: () -> Unit
) {
    val authState by authViewModel.state.collectAsState()
    val gamesState by gamesViewModel.state.collectAsState()
    val lessonsState by lessonsViewModel.state.collectAsState()

    val user = (authState as? AuthState.Authenticated)?.user ?: UserEntity(
        email = "student@udmurt.ru",
        firstName = "Ученик",
        lastName = "",
        gender = "Мужской",
        age = 7,
        role = "Ученик",
        avatar = "🦊"
    )

    val initialGames = gamesState as? GamesState.Initial
    val stats = initialGames?.stats
    val errorWords = initialGames?.topErrorWords ?: emptyList()

    val loadedLessons = lessonsState as? LessonsState.Loaded
    val totalWords = loadedLessons?.groupedWords?.values?.flatten()?.size ?: 75
    val viewedCount = loadedLessons?.progress?.viewedWordIds?.size ?: 0
    val progressPercent = if (totalWords > 0) ((viewedCount.toFloat() / totalWords) * 100).toInt() else 0

    var copiedSnackbar by remember { mutableStateOf(false) }

    QuizScaffold(
        title = "Отчёт для родителей и учителей",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = UdmurtRed)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Сводка успеваемости",
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Приложение «Ӟеч-а бур-а удмурт кыл?»",
                                color = White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ученик", color = White.copy(alpha = 0.75f), fontSize = 11.sp)
                            Text("${user.firstName} ${user.lastName}".trim().ifEmpty { "Ученик" }, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Возраст", color = White.copy(alpha = 0.75f), fontSize = 11.sp)
                            Text("${user.age} лет", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Статус", color = White.copy(alpha = 0.75f), fontSize = 11.sp)
                            Text(user.role, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Curriculum Mastery
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Освоение учебной программы",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Изучено слов: $viewedCount из $totalWords", fontSize = 14.sp)
                        Text("$progressPercent%", fontWeight = FontWeight.Bold, color = UdmurtRed, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (totalWords > 0) viewedCount.toFloat() / totalWords else 0f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = UdmurtRed,
                        trackColor = UdmurtRed.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Accuracy
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Результаты в мини-играх",
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
                            Text("Всего попыток", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${stats?.totalAnswered ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Успешных ответов", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${stats?.totalCorrect ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorrectGreen)
                        }
                        Column {
                            Text("Общая точность", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${((stats?.accuracy ?: 0.0) * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UdmurtRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pedagogical Recommendation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UdmurtGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Методические рекомендации:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val recommendation = if (errorWords.isNotEmpty()) {
                        "Рекомендуется уделить внимание словам: ${errorWords.take(3).joinToString(", ") { "«${it.udmurt}» (${it.russian})" }}. Лучше всего повторить их в играх «Выбери перевод» и «Собери слово»."
                    } else if (viewedCount < 15) {
                        "Ученик успешно начал обучение! Рекомендуется ежедневно проходить по 1 теме из словаря и закреплять материал в мини-играх."
                    } else {
                        "Превосходный прогресс! Ребёнок уверенно усваивает базовую лексику удмуртского языка. Рекомендуется переходить к изучению фраз и диалогов."
                    }
                    Text(
                        text = recommendation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            T2Button(
                text = if (copiedSnackbar) "✓ Сводка скопирована!" else "Поделиться отчётом",
                onClick = {
                    copiedSnackbar = true
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = UdmurtRed
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

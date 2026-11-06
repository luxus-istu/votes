package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.luxus.votes.core.clipboard.copyText
import ru.luxus.votes.core.theme.LocalExtendedColors
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.auth.domain.UserModel
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
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val gamesState by gamesViewModel.state.collectAsStateWithLifecycle()
    val lessonsState by lessonsViewModel.state.collectAsStateWithLifecycle()
    val extended = LocalExtendedColors.current

    val user = (authState as? AuthState.Authenticated)?.user ?: UserModel(
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
    val progressPercent = if (totalWords > 0) {
        ((viewedCount.toFloat() / totalWords) * 100).toInt()
    } else {
        0
    }

    var copiedSnackbar by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    QuizScaffold(
        title = "Отчёт для родителей и учителей",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Отчёт об обучении",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Приложение «Ӟеч-а бур-а удмурт кыл?»",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportStat(
                            label = "Ученик",
                            value = "${user.firstName} ${user.lastName}".trim()
                                .ifEmpty { "Ученик" },
                            onPrimary = true,
                            modifier = Modifier.weight(1.4f)
                        )
                        ReportStat(
                            label = "Возраст",
                            value = "${user.age} лет",
                            onPrimary = true,
                            modifier = Modifier.weight(0.8f)
                        )
                        ReportStat(
                            label = "Статус",
                            value = user.role,
                            onPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ReportCard(title = "Слова") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выучено $viewedCount из $totalWords слов",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = {
                        if (totalWords > 0) viewedCount.toFloat() / totalWords else 0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReportCard(title = "Игры") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStat(
                        label = "Ответов",
                        value = "${stats?.totalAnswered ?: 0}",
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStat(
                        label = "Верно",
                        value = "${stats?.totalCorrect ?: 0}",
                        valueColor = extended.success,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStat(
                        label = "Точность",
                        value = "${((stats?.accuracy ?: 0.0) * 100).toInt()}%",
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Что повторить",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val recommendation = if (errorWords.isNotEmpty()) {
                        "Повторите с ребёнком слова " +
                            errorWords.take(3).joinToString(", ") {
                                "«${it.udmurt}» (${it.russian})"
                            } +
                            ". Закрепите их в играх «Выбери перевод» и «Собери слово»."
                    } else if (viewedCount < 15) {
                        "Ребёнок только начал. Пусть проходит по одной теме в день и закрепляет слова в играх."
                    } else {
                        "Ребёнок выучил базовые слова. Переходите к теме «Фразы» и коротким диалогам."
                    }
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            T2Button(
                text = if (copiedSnackbar) "Отчёт скопирован" else "Скопировать отчёт",
                onClick = {
                    val report = buildString {
                        appendLine("Отчёт об обучении")
                        appendLine("Ученик: ${user.firstName} ${user.lastName}".trim())
                        appendLine("Возраст: ${user.age}")
                        appendLine("Выучено слов: $viewedCount из $totalWords ($progressPercent%)")
                        appendLine("Ответов: ${stats?.totalAnswered ?: 0}")
                        appendLine("Верных: ${stats?.totalCorrect ?: 0}")
                        appendLine("Точность: ${((stats?.accuracy ?: 0.0) * 100).toInt()}%")
                        if (errorWords.isNotEmpty()) {
                            append("Слова с ошибками: ")
                            append(
                                errorWords.take(5).joinToString(", ") {
                                    "${it.udmurt} (${it.russian})"
                                }
                            )
                        }
                    }
                    scope.launch {
                        clipboard.copyText(report.trim())
                        copiedSnackbar = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReportStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onPrimary: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (onPrimary) {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (onPrimary) MaterialTheme.colorScheme.onPrimary else valueColor,
            maxLines = 2,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun ReportCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
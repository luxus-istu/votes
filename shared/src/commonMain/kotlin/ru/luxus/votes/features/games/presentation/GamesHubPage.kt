package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.Magenta
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold

@Composable
fun GamesHubPage(
    viewModel: GamesViewModel,
    onNavigateToGame: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(GamesEvent.LoadStats)
    }

    QuizScaffold(
        title = "Игры и Викторины",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val stats = (state as? GamesState.Initial)?.stats

            if (stats != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Magenta)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Ваш прогресс",
                            color = White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Всего ответов", color = White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("${stats.totalAnswered}", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Правильно", color = White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("${stats.totalCorrect}", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Точность", color = White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("${(stats.accuracy * 100).toInt()}%", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Выберите режим:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            GameModeCard(
                title = "Выбери перевод",
                description = "4 варианта перевода для каждого удмуртского слова",
                emoji = "🎯",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("translate"))
                    onNavigateToGame("translate")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameModeCard(
                title = "Собери слово",
                description = "Составь удмуртское слово из перемешанных букв",
                emoji = "🔤",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("compile"))
                    onNavigateToGame("compile")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameModeCard(
                title = "Правда или Ложь",
                description = "Быстро оценивай верность перевода пар слов",
                emoji = "⚡",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("truefalse"))
                    onNavigateToGame("truefalse")
                }
            )
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
                    .size(52.dp)
                    .background(Magenta.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

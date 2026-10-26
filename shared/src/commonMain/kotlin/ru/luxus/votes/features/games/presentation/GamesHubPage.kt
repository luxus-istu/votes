package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
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
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.features.games.domain.GAME_UNLOCKS
import ru.luxus.votes.features.games.domain.GameUnlock

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
            val initial = state as? GamesState.Initial
            val stats = initial?.stats
            val learnedWords = initial?.viewedWordIds?.size ?: 0

            if (stats != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = UdmurtRed)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Игровой прогресс",
                                color = White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
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
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "Все 6 обучающих игр:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Игры открываются по мере изучения слов. " +
                    "Сначала пройди карточки в разделе «Обучение», а потом проверь знания в игре.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            GAME_UNLOCKS.forEachIndexed { index, game ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                GameModeCard(
                    game = game,
                    learnedWords = learnedWords,
                    onClick = {
                        viewModel.onEvent(GamesEvent.StartGame(game.key))
                        onNavigateToGame(game.key)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GameModeCard(
    game: GameUnlock,
    learnedWords: Int,
    onClick: () -> Unit
) {
    val unlocked = learnedWords >= game.requiredWords
    val remaining = (game.requiredWords - learnedWords).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = unlocked, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        UdmurtGold.copy(alpha = if (unlocked) 0.2f else 0.1f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unlocked) game.emoji else "🔒",
                    fontSize = 28.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (unlocked) 1f else 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (unlocked) {
                        game.description
                    } else {
                        "Изучи ещё $remaining ${wordsWord(remaining)}, чтобы открыть игру"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )

                if (!unlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = {
                            (learnedWords.toFloat() / game.requiredWords)
                                .coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = UdmurtRed,
                        trackColor = UdmurtRed.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Изучено $learnedWords из ${game.requiredWords} слов",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = UdmurtRed
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (unlocked) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Lock,
                contentDescription = if (unlocked) "Открыть игру" else "Игра закрыта",
                tint = if (unlocked) UdmurtRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun wordsWord(count: Int): String {
    val lastTwo = count % 100
    val last = count % 10
    return when {
        lastTwo in 11..14 -> "слов"
        last == 1 -> "слово"
        last in 2..4 -> "слова"
        else -> "слов"
    }
}

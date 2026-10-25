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
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
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

            Spacer(modifier = Modifier.height(14.dp))

            // Game 1: Choose Translation
            GameModeCard(
                title = "1. Выбери перевод",
                description = "Выбери правильный русский перевод из 4 вариантов",
                emoji = "🎯",
                badge = "Быстрый тест",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("translate"))
                    onNavigateToGame("translate")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Game 2: Find the Odd One Out
            GameModeCard(
                title = "2. Найди лишнее слово",
                description = "4 слова на удмуртском: три из одной темы, а одно — лишнее!",
                emoji = "🔍",
                badge = "Логика",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("oddoneout"))
                    onNavigateToGame("oddoneout")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Game 3: Sorting
            GameModeCard(
                title = "3. Сортировка",
                description = "Разложи слова и картинки по категориям в правильные корзины",
                emoji = "🧺",
                badge = "Категории",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("sorting"))
                    onNavigateToGame("sorting")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Game 4: Find the Pair (Memory)
            GameModeCard(
                title = "4. Найди пару (Memory)",
                description = "Открывай закрытые карточки и находи пары: слово — перевод",
                emoji = "🃏",
                badge = "Память",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("memory"))
                    onNavigateToGame("memory")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Game 5: Put Word Together
            GameModeCard(
                title = "5. Собери слово",
                description = "Составь удмуртское слово из перемешанных букв",
                emoji = "🔤",
                badge = "Буквы",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("compile"))
                    onNavigateToGame("compile")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Game 6: True / False
            GameModeCard(
                title = "6. Правда или Ложь",
                description = "Оцени, верно ли слово соответствует картинке и переводу",
                emoji = "⚡",
                badge = "Реакция",
                onClick = {
                    viewModel.onEvent(GamesEvent.StartGame("truefalse"))
                    onNavigateToGame("truefalse")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    emoji: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
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
                    .size(54.dp)
                    .background(UdmurtGold.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UdmurtRed.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = UdmurtRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

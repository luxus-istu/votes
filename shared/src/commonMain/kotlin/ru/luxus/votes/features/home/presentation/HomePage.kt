package ru.luxus.votes.features.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.features.auth.presentation.AuthState
import ru.luxus.votes.features.auth.presentation.AuthViewModel
import ru.luxus.votes.features.games.presentation.GamesEvent
import ru.luxus.votes.features.games.presentation.GamesState
import ru.luxus.votes.features.games.presentation.GamesViewModel
import votes.shared.generated.resources.Res
import votes.shared.generated.resources.mascot_star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    authViewModel: AuthViewModel,
    gamesViewModel: GamesViewModel,
    onNavigateToGames: () -> Unit,
    onNavigateToLessons: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val authState by authViewModel.state.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    val gamesState by gamesViewModel.state.collectAsState()
    val initialGames = gamesState as? GamesState.Initial
    val wordOfTheDay = initialGames?.wordOfTheDay
    val viewedWordIds = initialGames?.viewedWordIds.orEmpty()
    val isWordLearned = wordOfTheDay != null && wordOfTheDay.id in viewedWordIds

    LaunchedEffect(Unit) {
        gamesViewModel.loadStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ӟеч-а бур-а удмурт кыл?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (user != null) "Ӟечбуресь, ${user.firstName}! 👋" else "Ӟечбуресь! 👋",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Welcome Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = UdmurtRed)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            top = 20.dp,
                            bottom = 20.dp,
                            end = 100.dp
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Учи удмуртский с радостью!",
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Интерактивный самоучитель для детей 5–10 лет: словарь, озвучка и 6 весёлых мини-игр",
                            color = White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // Mascot bleeds into the whole bottom-right corner
                    Image(
                        painter = painterResource(Res.drawable.mascot_star),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .height(124.dp)
                            .aspectRatio(414f / 418f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Learning pace & today's goal (set by the parent on the dashboard)
            if (initialGames != null) {
                val goal = initialGames.dailyWordGoal.coerceAtLeast(1)
                // Never show more than the goal, even if older builds inflated
                // the stored counter by counting the same word repeatedly.
                val learnedToday = initialGames.wordsLearnedToday.coerceIn(0, goal)
                val goalFraction = (learnedToday.toFloat() / goal).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(UdmurtGold.copy(alpha = 0.22f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎈", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Мой уровень: ${initialGames.learningPace.lowercase()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Сегодня $learnedToday из $goal новых слов",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                            Text(
                                text = "${(goalFraction * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = UdmurtRed
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { goalFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (goalFraction >= 1f) CorrectGreen else UdmurtGold,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Word of the Day (Слово дня)
            if (wordOfTheDay != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = UdmurtGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "🌟 СЛОВО ДНЯ",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = UdmurtRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "«${wordOfTheDay.category}»",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            IconButton(
                                onClick = {
                                    gamesViewModel.onEvent(
                                        GamesEvent.PlayAudio(
                                            wordOfTheDay.udmurt,
                                            wordOfTheDay.transcription
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(UdmurtRed.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Прослушать слово дня",
                                    tint = UdmurtRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(wordOfTheDay.emoji, fontSize = 42.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wordOfTheDay.udmurt,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${wordOfTheDay.transcription} — ${wordOfTheDay.russian}",
                                    fontSize = 14.sp,
                                    color = UdmurtRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    val word = wordOfTheDay
                                    if (word != null && !isWordLearned) {
                                        gamesViewModel.onEvent(GamesEvent.MarkWordViewed(word.id))
                                    }
                                },
                                enabled = !isWordLearned,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UdmurtGold.copy(alpha = 0.3f),
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = CorrectGreen,
                                    disabledContentColor = White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isWordLearned) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Изучено", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Запомнить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "Главные разделы:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Нажми на карточку, чтобы открыть раздел",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Navigation Hub sections
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HomeDashboardCard(
                    title = "1. Обучение",
                    subtitle = "Словарь и карточки слов",
                    emoji = "📚",
                    onClick = onNavigateToLessons,
                    modifier = Modifier.weight(1f)
                )

                HomeDashboardCard(
                    title = "2. Игры",
                    subtitle = "Все 6 развивающих игр",
                    emoji = "🎮",
                    onClick = onNavigateToGames,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HomeDashboardCard(
                    title = "3. Профиль",
                    subtitle = "Кабинет, факты и отчёты",
                    emoji = "👤",
                    onClick = onNavigateToProfile,
                    modifier = Modifier.weight(1f)
                )

                HomeDashboardCard(
                    title = "4. Настройки",
                    subtitle = "Звук, уведомления и гео",
                    emoji = "⚙️",
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cultural Heritage footer card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏛️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Удмуртская Республика",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Программа поддержки и изучения языка (Постановление № 748)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HomeDashboardCard(
    title: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(min = 150.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 30.sp)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(UdmurtRed.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Открыть раздел",
                        tint = UdmurtRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

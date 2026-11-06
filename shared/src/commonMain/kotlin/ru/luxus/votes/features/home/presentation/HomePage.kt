package ru.luxus.votes.features.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.LocalExtendedColors
import ru.luxus.votes.features.auth.presentation.AuthState
import ru.luxus.votes.features.games.domain.WordEntry
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
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val user = (authState as? AuthState.Authenticated)?.user

    val gamesState by gamesViewModel.state.collectAsStateWithLifecycle()
    val initialGames = gamesState as? GamesState.Initial
    val wordOfTheDay = initialGames?.wordOfTheDay
    val viewedWordIds = initialGames?.viewedWordIds.orEmpty()
    val isWordLearned = wordOfTheDay != null && wordOfTheDay.id in viewedWordIds

    LaunchedEffect(Unit) {
        gamesViewModel.onEvent(GamesEvent.LoadStats)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ӟеч-а бур-а удмурт кыл?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (user != null) "Ӟечбуресь, ${user.firstName}!" else "Ӟечбуресь!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                WelcomeHero()

                Spacer(modifier = Modifier.height(18.dp))

                // Learning pace & today's goal (set by the parent on the dashboard)
                if (initialGames != null) {
                    val goal = initialGames.dailyWordGoal.coerceAtLeast(1)
                    // Never show more than the goal, even if older builds inflated
                    // the stored counter by counting the same word repeatedly.
                    val learnedToday = initialGames.wordsLearnedToday.coerceIn(0, goal)
                    val goalFraction = (learnedToday.toFloat() / goal).coerceIn(0f, 1f)
                    val successColor = LocalExtendedColors.current.success
                    val goalReached = goalFraction >= 1f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Темп занятий: ${initialGames.learningPace.lowercase()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Сегодня выучено $learnedToday из $goal слов",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${(goalFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (goalReached) successColor else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { goalFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(MaterialTheme.shapes.extraSmall),
                                color = if (goalReached) successColor else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                if (wordOfTheDay != null) {
                    WordOfTheDayCard(
                        word = wordOfTheDay,
                        isWordLearned = isWordLearned,
                        onPlayAudio = {
                            gamesViewModel.onEvent(
                                GamesEvent.PlayAudio(
                                    wordOfTheDay.udmurt,
                                    wordOfTheDay.transcription
                                )
                            )
                        },
                        onMarkLearned = {
                            if (!isWordLearned) {
                                gamesViewModel.onEvent(GamesEvent.MarkWordViewed(wordOfTheDay.id))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                    text = "Разделы",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HomeDashboardCard(
                        title = "Обучение",
                        subtitle = "Словарь и карточки слов",
                        emoji = "📚",
                        onClick = onNavigateToLessons,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    HomeDashboardCard(
                        title = "Игры",
                        subtitle = "Шесть игр для закрепления слов",
                        emoji = "🎮",
                        onClick = onNavigateToGames,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HomeDashboardCard(
                        title = "Профиль",
                        subtitle = "Аватар, статистика и отчёты",
                        emoji = "👤",
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    HomeDashboardCard(
                        title = "Настройки",
                        subtitle = "Звук, уведомления и геолокация",
                        emoji = "⚙️",
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun WelcomeHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 20.dp,
                    bottom = 20.dp,
                    end = 108.dp
                )
            ) {
                Text(
                    text = "Учи удмуртский",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Карточки со словами, озвучка и шесть игр для детей от 5 до 10 лет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }

            // Decorative mascot, bleeding into the bottom-right corner.
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
}

@Composable
private fun WordOfTheDayCard(
    word: WordEntry,
    isWordLearned: Boolean,
    onPlayAudio: () -> Unit,
    onMarkLearned: () -> Unit
) {
    val extended = LocalExtendedColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Слово дня",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = word.category.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                FilledTonalIconButton(
                    onClick = onPlayAudio,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Прослушать слово дня",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.emoji,
                    modifier = Modifier.clearAndSetSemantics { },
                    fontSize = 42.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.udmurt,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${word.transcription} · ${word.russian}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isWordLearned) {
                    Surface(
                        shape = CircleShape,
                        color = extended.successContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = extended.onSuccessContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Изучено",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = extended.onSuccessContainer
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = onMarkLearned,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Запомнить",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
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
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    modifier = Modifier.clearAndSetSemantics { },
                    fontSize = 30.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
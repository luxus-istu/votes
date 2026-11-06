package ru.luxus.votes.features.lessons.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.abs
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.LocalExtendedColors
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.games.domain.unlockedGameKeys
import votes.shared.generated.resources.Res
import votes.shared.generated.resources.mascot_star

@Composable
fun LessonCardsPage(
    topic: String,
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
    onPlayRandomGame: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val extended = LocalExtendedColors.current

    val loadedState = state as? LessonsState.Loaded
    val categoryWords = loadedState?.groupedWords?.get(topic) ?: emptyList()
    val viewedIds = loadedState?.progress?.viewedWordIds.orEmpty()
    // Only offer games the child has already unlocked through studying words.
    val availableGames = unlockedGameKeys(viewedIds.size)

    val learnedCount = categoryWords.count { it.id in viewedIds }
    val topicComplete = categoryWords.isNotEmpty() && learnedCount == categoryWords.size

    var showVictory by remember(topic) { mutableStateOf(false) }

    QuizScaffold(
        title = topic.replaceFirstChar { it.uppercase() },
        onBack = onBack
    ) {
        if (categoryWords.isNotEmpty()) {
            val startPage = remember(categoryWords.size) {
                if (categoryWords.isEmpty()) 0
                else (Int.MAX_VALUE / 2 / categoryWords.size) * categoryWords.size
            }
            val pagerState = rememberPagerState(
                initialPage = startPage,
                pageCount = { Int.MAX_VALUE }
            )
            val currentPage = pagerState.currentPage
            var flippedPage by remember { mutableStateOf(-1) }

            // Reset the translation reveal when rotating to another card
            LaunchedEffect(currentPage) {
                flippedPage = -1
            }

            // Header stays visible on the victory slide too
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showVictory) {
                        "Все слова изучены!"
                    } else {
                        "Изучено $learnedCount из ${categoryWords.size}"
                    },
                    modifier = Modifier.semantics {
                        if (showVictory) heading()
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (showVictory) FontWeight.Bold else FontWeight.Normal,
                    color = if (showVictory) {
                        extended.success
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { learnedCount.toFloat() / categoryWords.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = if (topicComplete) {
                    extended.success
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showVictory) {
                TopicVictorySlide(
                    topic = topic,
                    wordCount = categoryWords.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onTestKnowledge = {
                        onPlayRandomGame(
                            availableGames.ifEmpty { listOf("translate") }.random()
                        )
                    },
                    onBackToTopics = onBack
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 0.dp,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val wordIndex = ((page % categoryWords.size) + categoryWords.size) %
                        categoryWords.size
                    val word = categoryWords[wordIndex]
                    val pageOffset = (pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction
                    val distance = abs(pageOffset).coerceAtMost(2.5f)

                    WordCard(
                        word = word,
                        isFlipped = flippedPage == page,
                        isViewed = word.id in viewedIds,
                        showFinishButton = wordIndex == categoryWords.lastIndex && topicComplete,
                        onFinish = { showVictory = true },
                        onFlip = {
                            val revealing = flippedPage != page
                            flippedPage = if (revealing) page else -1
                            // A word counts as studied once the child reveals its
                            // translation – merely swiping past it is not enough
                            if (revealing) {
                                viewModel.onEvent(LessonsEvent.MarkWordViewed(word.id))
                            }
                        },
                        onPlayAudio = {
                            viewModel.onEvent(
                                LessonsEvent.PlayAudio(word.udmurt, word.transcription)
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // Arc / cover-flow transform: cards rotate around a circle
                                rotationY = pageOffset * 26f
                                cameraDistance = 16f * density
                                translationY = distance * 18f
                                val scale = (1f - distance * 0.12f).coerceIn(0.72f, 1f)
                                scaleX = scale
                                scaleY = scale
                                alpha = (1f - distance * 0.28f).coerceIn(0.35f, 1f)
                            }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Листай карточки",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WordCard(
    word: WordEntry,
    isFlipped: Boolean,
    isViewed: Boolean,
    showFinishButton: Boolean,
    onFinish: () -> Unit,
    onFlip: () -> Unit,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalExtendedColors.current

    Card(
        onClick = onFlip,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = word.emoji,
                modifier = Modifier.clearAndSetSemantics { },
                fontSize = 72.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = word.udmurt,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = word.transcription,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalIconButton(
                onClick = onPlayAudio,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Прослушать произношение",
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isFlipped) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Перевод: ${word.russian}",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Нажми на карточку, чтобы увидеть перевод",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            if (isViewed) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = extended.successContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = extended.onSuccessContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Слово изучено",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extended.onSuccessContainer
                        )
                    }
                }
            }

            if (showFinishButton) {
                Spacer(modifier = Modifier.height(16.dp))
                T2Button(
                    text = "Завершить тему",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = extended.success,
                    contentColor = extended.onSuccess
                )
            }
        }
    }
}

@Composable
private fun TopicVictorySlide(
    topic: String,
    wordCount: Int,
    modifier: Modifier = Modifier,
    onTestKnowledge: () -> Unit,
    onBackToTopics: () -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.mascot_star),
            contentDescription = null,
            modifier = Modifier
                .height(160.dp)
                .aspectRatio(414f / 418f),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Тема пройдена",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Все $wordCount слов темы «${topic.replaceFirstChar { it.uppercase() }}» изучены. Проверь знания в игре.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        T2Button(
            text = "Проверить знания",
            onClick = onTestKnowledge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onBackToTopics) {
            Text(
                text = "К темам",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
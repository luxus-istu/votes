package ru.luxus.votes.features.lessons.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.games.domain.WordEntry
import votes.shared.generated.resources.Res
import votes.shared.generated.resources.mascot_star

private val RANDOM_GAMES = listOf(
    "translate", "oddoneout", "sorting", "memory", "compile", "truefalse"
)

@Composable
fun LessonCardsPage(
    topic: String,
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
    onPlayRandomGame: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val loadedState = state as? LessonsState.Loaded
    val categoryWords = loadedState?.groupedWords?.get(topic) ?: emptyList()
    val viewedIds = loadedState?.progress?.viewedWordIds.orEmpty()

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
            var lastStudiedPage by remember { mutableIntStateOf(-1) }

            // Every card the child rotates past counts as studied
            LaunchedEffect(currentPage, categoryWords) {
                flippedPage = -1
                val from = if (lastStudiedPage < 0) currentPage
                else minOf(lastStudiedPage, currentPage)
                val to = if (lastStudiedPage < 0) currentPage
                else maxOf(lastStudiedPage, currentPage)
                for (page in from..to) {
                    categoryWords[((page % categoryWords.size) + categoryWords.size) % categoryWords.size]
                        .let { word -> viewModel.onEvent(LessonsEvent.MarkWordViewed(word.id)) }
                }
                lastStudiedPage = currentPage
            }

            // Header stays visible on the victory slide too
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showVictory) "Все слова изучены!"
                    else "Изучено $learnedCount из ${categoryWords.size}",
                    fontSize = 14.sp,
                    fontWeight = if (showVictory) FontWeight.Bold else FontWeight.Normal,
                    color = if (showVictory) CorrectGreen
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { learnedCount.toFloat() / categoryWords.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (topicComplete) CorrectGreen else UdmurtRed,
                trackColor = UdmurtRed.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showVictory) {
                TopicVictorySlide(
                    topic = topic,
                    wordCount = categoryWords.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onTestKnowledge = { onPlayRandomGame(RANDOM_GAMES.random()) },
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
                    val wordIndex = ((page % categoryWords.size) + categoryWords.size) % categoryWords.size
                    val word = categoryWords[wordIndex]
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val distance = abs(pageOffset).coerceAtMost(2.5f)

                    WordCard(
                        word = word,
                        isFlipped = flippedPage == page,
                        isViewed = word.id in viewedIds,
                        showFinishButton = wordIndex == categoryWords.lastIndex && topicComplete,
                        onFinish = { showVictory = true },
                        onFlip = { flippedPage = if (flippedPage == page) -1 else page },
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
                    text = "👈 Листайте по кругу 👉",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
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
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onFlip),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(word.emoji, fontSize = 72.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = word.udmurt,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = UdmurtRed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = word.transcription,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = onPlayAudio,
                modifier = Modifier
                    .size(50.dp)
                    .background(UdmurtGold.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Прослушать произношение",
                    tint = UdmurtRed,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isFlipped) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = UdmurtRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Перевод: ${word.russian}",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "👆 Нажмите на карточку, чтобы открыть перевод",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (isViewed) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CorrectGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "✓ Слово изучено",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = CorrectGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (showFinishButton) {
                Spacer(modifier = Modifier.height(16.dp))
                T2Button(
                    text = "Я всё изучил(а)!",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CorrectGreen
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
        Text("🎉", fontSize = 44.sp)

        Image(
            painter = painterResource(Res.drawable.mascot_star),
            contentDescription = null,
            modifier = Modifier
                .height(160.dp)
                .aspectRatio(414f / 418f),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Тема пройдена!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = UdmurtRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Все $wordCount слов темы «${topic.replaceFirstChar { it.uppercase() }}» изучены. Проверим знания в игре?",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        T2Button(
            text = "Проверить знания",
            onClick = onTestKnowledge,
            modifier = Modifier.fillMaxWidth(),
            containerColor = UdmurtRed
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onBackToTopics) {
            Text(
                text = "К темам",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

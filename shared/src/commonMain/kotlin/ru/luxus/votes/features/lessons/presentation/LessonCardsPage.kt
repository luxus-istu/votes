package ru.luxus.votes.features.lessons.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.Magenta
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button

@Composable
fun LessonCardsPage(
    topic: String,
    viewModel: LessonsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val loadedState = state as? LessonsState.Loaded
    val categoryWords = loadedState?.groupedWords?.get(topic) ?: emptyList()

    var cardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    QuizScaffold(
        title = topic.replaceFirstChar { it.uppercase() },
        onBack = onBack
    ) {
        if (categoryWords.isNotEmpty()) {
            val word = categoryWords[cardIndex]

            Text(
                text = "Карточка ${cardIndex + 1} из ${categoryWords.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = cardIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width / 2 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally { width -> -width / 2 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { width -> -width / 2 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally { width -> width / 2 } + fadeOut(tween(200)))
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = "CardSlideAnimation"
            ) { targetIndex ->
                val targetWord = categoryWords[targetIndex]
                val isViewed = loadedState?.progress?.viewedWordIds?.contains(targetWord.id) == true

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { isFlipped = !isFlipped },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(targetWord.emoji, fontSize = 64.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = targetWord.udmurt,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Magenta,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = targetWord.transcription,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isFlipped) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Magenta.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Перевод: ${targetWord.russian}",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Text(
                                text = "Нажмите на карточку, чтобы посмотреть перевод",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        if (isViewed) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("✓ Изучено", color = CorrectGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (cardIndex > 0) {
                            cardIndex--
                            isFlipped = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = cardIndex > 0,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Назад")
                }

                T2Button(
                    text = "Изучено",
                    onClick = {
                        viewModel.onEvent(LessonsEvent.MarkWordViewed(word.id))
                        if (cardIndex < categoryWords.size - 1) {
                            cardIndex++
                            isFlipped = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = {
                        if (cardIndex < categoryWords.size - 1) {
                            cardIndex++
                            isFlipped = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = cardIndex < categoryWords.size - 1,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Вперед")
                }
            }
        }
    }
}

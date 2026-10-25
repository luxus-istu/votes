package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold

@Composable
fun SortingGamePage(
    viewModel: GamesViewModel,
    onGameComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is GamesState.SortingInProgress -> {
            val currentItem = current.items.getOrNull(current.currentItemIndex)

            QuizScaffold(
                title = "Сортировка (${current.currentItemIndex + 1}/${current.total})",
                onBack = onBack
            ) {
                LinearProgressIndicator(
                    progress = { (current.currentItemIndex + 1).toFloat() / current.total },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = UdmurtRed,
                    trackColor = UdmurtRed.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (current.lastFeedback != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (current.lastFeedback.startsWith("✓")) CorrectGreen.copy(alpha = 0.15f) else UdmurtRed.copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = current.lastFeedback,
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (current.lastFeedback.startsWith("✓")) CorrectGreen else UdmurtRed
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Card to sort
                if (currentItem != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(currentItem.emoji, fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentItem.udmurt,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentItem.transcription,
                                fontSize = 16.sp,
                                color = UdmurtRed,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "«${currentItem.russian}»",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            IconButton(
                                onClick = {
                                    viewModel.onEvent(
                                        GamesEvent.PlayAudio(
                                            currentItem.udmurt,
                                            currentItem.transcription
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(UdmurtGold.copy(alpha = 0.25f), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Прослушать",
                                    tint = UdmurtRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Отправь карточку в правильную корзину:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Two destination category buckets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryBucketCard(
                        categoryName = current.categoryA,
                        color = UdmurtRed,
                        onClick = { viewModel.onEvent(GamesEvent.AnswerSorting(current.categoryA)) },
                        modifier = Modifier.weight(1f)
                    )

                    CategoryBucketCard(
                        categoryName = current.categoryB,
                        color = UdmurtGold,
                        onClick = { viewModel.onEvent(GamesEvent.AnswerSorting(current.categoryB)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        is GamesState.QuizComplete -> {
            onGameComplete()
        }

        else -> {}
    }
}

@Composable
private fun CategoryBucketCard(
    categoryName: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(2.dp, color, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = categoryName.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

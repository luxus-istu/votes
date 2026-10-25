package ru.luxus.votes.features.games.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
fun MemoryGamePage(
    viewModel: GamesViewModel,
    onGameComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is GamesState.MemoryInProgress -> {
            QuizScaffold(
                title = "Найди пару (Memory)",
                onBack = onBack
            ) {
                // Header with stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Найдено пар",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${current.matchesFound} из ${current.totalPairs}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = UdmurtRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Сделано ходов",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${current.moves}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cards grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    itemsIndexed(current.cards) { index, card ->
                        val isRevealed = card.isFlipped || card.isMatched

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isRevealed && !current.isLocked) {
                                    viewModel.onEvent(GamesEvent.FlipMemoryCard(index))
                                }
                                .then(
                                    if (card.isMatched) {
                                        Modifier.border(2.dp, CorrectGreen, RoundedCornerShape(16.dp))
                                    } else Modifier
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isRevealed) {
                                    if (card.isMatched) CorrectGreen.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    UdmurtRed
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isRevealed) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (card.emoji.isNotBlank()) {
                                            Text(text = card.emoji, fontSize = 28.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(
                                            text = card.text,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (card.isMatched) CorrectGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (card.subtext.isNotBlank()) {
                                            Text(
                                                text = card.subtext,
                                                fontSize = 11.sp,
                                                color = UdmurtRed,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("🦊", fontSize = 30.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        is GamesState.QuizComplete -> {
            onGameComplete()
        }

        else -> {}
    }
}

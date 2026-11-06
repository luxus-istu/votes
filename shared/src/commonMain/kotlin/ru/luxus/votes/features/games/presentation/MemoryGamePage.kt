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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.luxus.votes.core.theme.LocalExtendedColors
import ru.luxus.votes.core.widgets.QuizScaffold

@Composable
fun MemoryGamePage(
    viewModel: GamesViewModel,
    onGameComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val extended = LocalExtendedColors.current

    when (val current = state) {
        is GamesState.MemoryInProgress -> {
            QuizScaffold(
                title = "Найди пару",
                onBack = onBack
            ) {
                // Header with stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Найдено пар",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${current.matchesFound} из ${current.totalPairs}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Ходы",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${current.moves}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cards grid. The column count adapts to the available width:
                // small phones get 2–3 columns of comfortable cards, large
                // tablets fit more columns instead of stretching the cards.
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(current.cards) { index, card ->
                        val isRevealed = card.isFlipped || card.isMatched
                        val rotation by animateFloatAsState(
                            targetValue = if (isRevealed) 180f else 0f,
                            animationSpec = tween(durationMillis = 380),
                            label = "memoryCardFlip"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                // Height scales with the card width on every
                                // screen size, so the board keeps fitting on
                                // small phones and avoids huge blank cards on
                                // tablets.
                                .aspectRatio(0.78f)
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(
                                    enabled = !isRevealed && !current.isLocked
                                ) {
                                    viewModel.onEvent(GamesEvent.FlipMemoryCard(index))
                                }
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = when {
                                        card.isMatched -> "Пара найдена: ${card.text}"
                                        card.isFlipped -> "Открыта карточка: ${card.text}"
                                        else -> "Закрытая карточка"
                                    }
                                }
                        ) {
                            if (rotation > 90f) {
                                // Front face – counter-rotated so the text is not mirrored
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            if (card.isMatched) {
                                                extended.successContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .then(
                                            if (card.isMatched) {
                                                Modifier.border(
                                                    2.dp,
                                                    extended.success,
                                                    MaterialTheme.shapes.medium
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (card.emoji.isNotBlank()) {
                                            Text(
                                                text = card.emoji,
                                                fontSize = 28.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(
                                            text = card.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (card.isMatched) {
                                                extended.onSuccessContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (card.subtext.isNotBlank()) {
                                            Text(
                                                text = card.subtext,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Card back
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🦊", fontSize = 30.sp)
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
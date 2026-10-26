package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button

@Composable
fun CompileWordPage(
    viewModel: GamesViewModel,
    onGameComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is GamesState.QuizInProgress -> {
            // Reset the picked letters synchronously when the word changes; a
            // LaunchedEffect would leave stale indices during the first frame
            var selectedIndices by remember(current.currentIndex) {
                mutableStateOf(listOf<Int>())
            }

            val assembledWord = selectedIndices
                .mapNotNull { current.scrambledLetters.getOrNull(it) }
                .joinToString("")

            QuizScaffold(
                title = "Собери слово (${current.currentIndex + 1}/${current.totalQuestions})",
                onBack = onBack
            ) {
                LinearProgressIndicator(
                    progress = { (current.currentIndex + 1).toFloat() / current.totalQuestions },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = UdmurtRed,
                    trackColor = UdmurtRed.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "«${current.question.russian}»",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        if (current.hintLevel >= 1) {
                            val cleanWord = current.question.udmurt.replace(" ", "")
                            val revealedLetters = when (current.hintLevel) {
                                1 -> (cleanWord.length + 2) / 3
                                2 -> (cleanWord.length * 2 + 2) / 3
                                else -> cleanWord.length
                            }.coerceIn(0, cleanWord.length)

                            val maskedWord = cleanWord.mapIndexed { index, char ->
                                if (index < revealedLetters) char else '•'
                            }.joinToString(" ")

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Подсказка: ${current.hintLevel} из 3 частей слова",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UdmurtRed,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = maskedWord,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp,
                                color = UdmurtRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        IconButton(
                            onClick = {
                                viewModel.onEvent(
                                    GamesEvent.PlayAudio(
                                        current.question.udmurt,
                                        current.question.transcription
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(UdmurtGold.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Прослушать",
                                tint = UdmurtRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (current.feedback != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IncorrectRed.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = current.feedback,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            color = IncorrectRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Word construction box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (assembledWord.isEmpty()) "Нажимай на буквы ниже" else assembledWord,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (assembledWord.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else UdmurtRed,
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedIndices.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                selectedIndices = selectedIndices.dropLast(1)
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Удалить букву",
                                tint = UdmurtRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Letter tiles
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(current.scrambledLetters) { index, char ->
                        val isUsed = selectedIndices.contains(index)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    else UdmurtGold.copy(alpha = 0.2f)
                                )
                                .clickable(enabled = !isUsed) {
                                    selectedIndices = selectedIndices + index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUsed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                T2Button(
                    text = "Проверить слово",
                    onClick = { viewModel.onEvent(GamesEvent.AnswerCompile(assembledWord)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = assembledWord.isNotEmpty(),
                    containerColor = UdmurtRed
                )
            }
        }

        is GamesState.QuizAnswered -> {
            QuizScaffold(
                title = "Результат ответа",
                onBack = onBack
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            if (current.isCorrect) CorrectGreen.copy(alpha = 0.15f) else IncorrectRed.copy(alpha = 0.15f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(current.previous.question.emoji, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (current.isCorrect) "🌟 Ӟеч! Ты собрал слово!" else "❌ Не совсем правильно",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (current.isCorrect) CorrectGreen else IncorrectRed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = current.explanation,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                T2Button(
                    text = "Следующее слово",
                    onClick = { viewModel.onEvent(GamesEvent.NextQuestion) },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = UdmurtRed
                )
            }
        }

        is GamesState.QuizComplete -> {
            onGameComplete()
        }

        else -> {}
    }
}

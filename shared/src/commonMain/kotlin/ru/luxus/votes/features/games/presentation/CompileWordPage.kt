package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.Magenta
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

    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    when (val current = state) {
        is GamesState.QuizInProgress -> {
            LaunchedEffect(current.currentIndex) {
                selectedIndices = emptySet()
            }

            val assembledWord = current.scrambledLetters
                .filterIndexed { index, _ -> selectedIndices.contains(index) }
                .joinToString("")

            QuizScaffold(
                title = "Собери слово (${current.currentIndex + 1}/${current.totalQuestions})",
                onBack = onBack
            ) {
                Text(
                    text = "Перевод: «${current.question.russian}» ${current.question.emoji}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (assembledWord.isEmpty()) "Нажимайте на буквы" else assembledWord,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (assembledWord.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else Magenta
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(current.scrambledLetters) { index, char ->
                        val isSelected = selectedIndices.contains(index)
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Magenta.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedIndices = if (isSelected) {
                                        selectedIndices - index
                                    } else {
                                        selectedIndices + index
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Magenta else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                T2Button(
                    text = "Проверить",
                    onClick = { viewModel.onEvent(GamesEvent.AnswerCompile(assembledWord)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = assembledWord.isNotEmpty()
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
                            RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (current.isCorrect) "🎉 Отлично!" else "❌ Ошибка",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (current.isCorrect) CorrectGreen else IncorrectRed
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Верно: «${current.correctAnswer}»",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                T2Button(
                    text = "Далее",
                    onClick = { viewModel.onEvent(GamesEvent.NextQuestion) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is GamesState.QuizComplete -> {
            onGameComplete()
        }

        else -> {}
    }
}

package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun ChooseTranslationPage(
    viewModel: GamesViewModel,
    onGameComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is GamesState.QuizInProgress -> {
            QuizScaffold(
                title = "Вопрос ${current.currentIndex + 1} из ${current.totalQuestions}",
                onBack = onBack
            ) {
                LinearProgressIndicator(
                    progress = { (current.currentIndex + 1).toFloat() / current.totalQuestions },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Magenta
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(current.question.emoji, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = current.question.udmurt,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = current.question.transcription,
                            fontSize = 14.sp,
                            color = Magenta
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Выберите правильный перевод:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                current.options.forEach { option ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.onEvent(GamesEvent.AnswerTranslate(option)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
                            text = if (current.isCorrect) "🎉 Правильно!" else "❌ Ошибка",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (current.isCorrect) CorrectGreen else IncorrectRed
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = current.correctAnswer,
                            fontSize = 18.sp,
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

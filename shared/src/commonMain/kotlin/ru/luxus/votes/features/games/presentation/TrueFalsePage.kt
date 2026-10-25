package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button

@Composable
fun TrueFalsePage(
    viewModel: GamesViewModel,
    onGameComplete: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is GamesState.QuizInProgress -> {
            QuizScaffold(
                title = "Правда или Ложь (${current.currentIndex + 1}/${current.totalQuestions})",
                onBack = onBack
            ) {
                LinearProgressIndicator(
                    progress = { (current.currentIndex + 1).toFloat() / current.totalQuestions },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = UdmurtRed,
                    trackColor = UdmurtRed.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(current.question.emoji, fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Означает ли слово",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«${current.options[0]}»",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = UdmurtRed,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(
                            onClick = {
                                viewModel.onEvent(
                                    GamesEvent.PlayAudio(
                                        current.options[0],
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "перевод: «${current.question.russian}»?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    T2Button(
                        text = "Ложь ❌",
                        onClick = { viewModel.onEvent(GamesEvent.AnswerTrueFalse(false)) },
                        modifier = Modifier.weight(1f),
                        containerColor = IncorrectRed
                    )

                    T2Button(
                        text = "Правда ✅",
                        onClick = { viewModel.onEvent(GamesEvent.AnswerTrueFalse(true)) },
                        modifier = Modifier.weight(1f),
                        containerColor = CorrectGreen
                    )
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
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (current.isCorrect) "🌟 Ӟеч! Верно!" else "❌ Ошибка!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (current.isCorrect) CorrectGreen else IncorrectRed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = current.correctAnswer,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                T2Button(
                    text = "Следующий вопрос",
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

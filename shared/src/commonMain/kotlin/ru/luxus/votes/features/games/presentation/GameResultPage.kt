package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button

@Composable
fun GameResultPage(
    viewModel: GamesViewModel,
    onBackToGames: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val complete = state as? GamesState.QuizComplete

    QuizScaffold(
        title = "Итоги игры",
        onBack = onBackToGames
    ) {
        if (complete != null) {
            val percentage = if (complete.total > 0) ((complete.score.toFloat() / complete.total) * 100).toInt() else 0

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = UdmurtRed)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (percentage >= 80) "🏆 Тау! Отличный результат!" else if (percentage >= 50) "👍 Ӟеч! Хорошая работа!" else "💪 Попробуй ещё раз!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$percentage%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Правильных ответов: ${complete.score} из ${complete.total}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Результаты сохранены в твоем профиле и отчёте!",
                        fontSize = 13.sp,
                        color = White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            T2Button(
                text = "Вернуться в раздел игр",
                onClick = onBackToGames,
                modifier = Modifier.fillMaxWidth(),
                containerColor = UdmurtRed
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UdmurtRed)
            }
        }
    }
}

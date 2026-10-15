package ru.luxus.votes.features.games.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import ru.luxus.votes.core.theme.Magenta
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
        title = "Итоги викторины",
        onBack = onBackToGames
    ) {
        if (complete != null) {
            val percentage = ((complete.score.toFloat() / complete.total) * 100).toInt()

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Magenta)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (percentage >= 80) "🏆 Отличный результат!" else if (percentage >= 50) "👍 Хорошая работа!" else "💪 Попробуйте еще раз!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(White.copy(alpha = 0.2f), RoundedCornerShape(100.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$percentage%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Вы ответили правильно на ${complete.score} из ${complete.total} вопросов",
                        fontSize = 16.sp,
                        color = White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            T2Button(
                text = "Вернуться в меню игр",
                onClick = onBackToGames,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Magenta)
            }
        }
    }
}

package ru.luxus.votes.features.lessons.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.widgets.QuizScaffold

private data class UdmurtLetter(val upper: String, val lower: String)

private val SPECIAL_LETTERS = setOf("Ӝ", "Ӟ", "Ӥ", "Ӧ", "Ӵ")

private val UDMURT_ALPHABET = listOf(
    UdmurtLetter("А", "а"), UdmurtLetter("Б", "б"), UdmurtLetter("В", "в"),
    UdmurtLetter("Г", "г"), UdmurtLetter("Д", "д"), UdmurtLetter("Е", "е"),
    UdmurtLetter("Ё", "ё"), UdmurtLetter("Ж", "ж"), UdmurtLetter("Ӝ", "ӝ"),
    UdmurtLetter("З", "з"), UdmurtLetter("Ӟ", "ӟ"), UdmurtLetter("И", "и"),
    UdmurtLetter("Ӥ", "ӥ"), UdmurtLetter("Й", "й"), UdmurtLetter("К", "к"),
    UdmurtLetter("Л", "л"), UdmurtLetter("М", "м"), UdmurtLetter("Н", "н"),
    UdmurtLetter("О", "о"), UdmurtLetter("Ӧ", "ӧ"), UdmurtLetter("П", "п"),
    UdmurtLetter("Р", "р"), UdmurtLetter("С", "с"), UdmurtLetter("Т", "т"),
    UdmurtLetter("У", "у"), UdmurtLetter("Ф", "ф"), UdmurtLetter("Х", "х"),
    UdmurtLetter("Ц", "ц"), UdmurtLetter("Ч", "ч"), UdmurtLetter("Ӵ", "ӵ"),
    UdmurtLetter("Ш", "ш"), UdmurtLetter("Щ", "щ"), UdmurtLetter("Ъ", "ъ"),
    UdmurtLetter("Ы", "ы"), UdmurtLetter("Ь", "ь"), UdmurtLetter("Э", "э"),
    UdmurtLetter("Ю", "ю"), UdmurtLetter("Я", "я"),
)

@Composable
fun AlphabetPage(
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
) {
    QuizScaffold(
        title = "Удмуртский алфавит",
        onBack = onBack
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = UdmurtGold.copy(alpha = 0.15f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "38 букв",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UdmurtRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Пять особых букв удмуртского алфавита: ӝ, ӟ, ӥ, ӧ, ӵ. " +
                        "Нажмите на любую букву, чтобы услышать её.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(UDMURT_ALPHABET) { letter ->
                val isSpecial = letter.upper in SPECIAL_LETTERS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSpecial) UdmurtGold.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .then(
                            if (isSpecial) {
                                Modifier.border(1.5.dp, UdmurtGold, RoundedCornerShape(16.dp))
                            } else Modifier
                        )
                        .clickable {
                            viewModel.onEvent(LessonsEvent.PlayAudio(letter.upper))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = letter.upper,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = UdmurtRed,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = letter.lower,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

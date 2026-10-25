package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold

@Composable
fun LanguageInfoPage(
    onBack: () -> Unit
) {
    QuizScaffold(
        title = "Об удмуртском языке",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = UdmurtRed)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Удмурт кыл",
                                color = White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Здравствуй, удмуртский язык!",
                                color = White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Один из государственных языков Удмуртской Республики. Приложение создано в рамках государственной программы сохранения и развития языка (Постановление Правительства УР № 748 от 20 ноября 2023 г.).",
                        color = White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key facts row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoStatCard(
                    title = "Носители",
                    value = "~324 000",
                    subtitle = "говорящих на языке",
                    modifier = Modifier.weight(1f)
                )

                InfoStatCard(
                    title = "Семья",
                    value = "Финно-угорская",
                    subtitle = "пермская ветвь",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Unique Alphabet letters
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Уникальные буквы алфавита",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "В удмуртском алфавите 38 букв: 33 русские и 5 особенных букв с двумя точками (диэрезисом):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AlphabetLetterBadge("Ӝ ӝ", "[дж]", "ӝук (каша)")
                        AlphabetLetterBadge("Ӟ ӟ", "[дзь]", "ӟеч (добро)")
                        AlphabetLetterBadge("Ӥ ӥ", "[и]", "одӥг (один)")
                        AlphabetLetterBadge("Ӧ ӧ", "[о]", "йӧл (молоко)")
                        AlphabetLetterBadge("Ӵ ӵ", "[тш]", "ӵукна (утро)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interesting facts
            Text(
                text = "Интересные факты:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            FactItem(
                emoji = "🥟",
                title = "Родина пельменей — Удмуртия",
                description = "Слово «пельмень» происходит от удмуртских слов «пель» (ухо) и «нянь» (хлеб) — то есть «хлебное ушко»!"
            )

            Spacer(modifier = Modifier.height(10.dp))

            FactItem(
                emoji = "☀️",
                title = "Солярный знак «Толэзё»",
                description = "Восьмиконечный знак на флаге Удмуртии оберегает от невзгод и олицетворяет солнце, жизнь и бесконечность."
            )

            Spacer(modifier = Modifier.height(10.dp))

            FactItem(
                emoji = "🌼",
                title = "Цветок Италмас",
                description = "Купальница европейская золотисто-жёлтого цвета — символ красоты, любви и вдохновения удмуртского народа."
            )

            Spacer(modifier = Modifier.height(10.dp))

            FactItem(
                emoji = "🌍",
                title = "Финно-угорские родственники",
                description = "Удмуртский язык родственен венгерскому, финскому, эстонскому, коми и марийскому языкам."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = UdmurtRed)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun AlphabetLetterBadge(
    letter: String,
    sound: String,
    example: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(UdmurtRed.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = UdmurtRed
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(sound, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun FactItem(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

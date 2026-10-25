package ru.luxus.votes.features.onboarding.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.widgets.T2Button
import votes.shared.generated.resources.Res
import votes.shared.generated.resources.mascot_book
import votes.shared.generated.resources.mascot_book_closeup
import votes.shared.generated.resources.mascot_book_left
import votes.shared.generated.resources.mascot_reading
import votes.shared.generated.resources.mascot_reading_left
import votes.shared.generated.resources.mascot_think
import votes.shared.generated.resources.mascot_think_closeup
import votes.shared.generated.resources.mascot_think_left
import votes.shared.generated.resources.mascot_wave_closeup

enum class OnboardingKind { APP, PARENT, CHILD }

private data class OnboardingStep(
    val emoji: String,
    val title: String,
    val description: String,
)

private fun stepsFor(kind: OnboardingKind): List<OnboardingStep> = when (kind) {
    OnboardingKind.APP -> listOf(
        OnboardingStep(
            emoji = "🎉",
            title = "Добро пожаловать!",
            description = "«Ӟеч-а бур-а удмурт кыл?» помогает детям 5–10 лет учить удмуртский язык играя."
        ),
        OnboardingStep(
            emoji = "📚",
            title = "Слова и игры",
            description = "Карточки слов по темам и шесть мини-игр: перевод, сортировка, memory, собери слово и другие."
        ),
        OnboardingStep(
            emoji = "👨‍👩‍👧‍👦",
            title = "Родитель и ребёнок",
            description = "Родитель создаёт профиль ребёнка и следит за успехами, а ребёнок занимается под своим логином."
        ),
    )

    OnboardingKind.PARENT -> listOf(
        OnboardingStep(
            emoji = "🧭",
            title = "Кабинет родителя",
            description = "Здесь собраны все ваши дети: профили, прогресс и статистика в одном месте."
        ),
        OnboardingStep(
            emoji = "➕",
            title = "Профили детей",
            description = "Добавьте ребёнка, укажите дату рождения и придумайте логин и пароль для его входа."
        ),
        OnboardingStep(
            emoji = "📈",
            title = "Успехи и отчёты",
            description = "Смотрите освоение тем, ошибки и успеваемость по играм — и копируйте отчёт для учителя."
        ),
    )

    OnboardingKind.CHILD -> listOf(
        OnboardingStep(
            emoji = "🚀",
            title = "Привет! Начнём?",
            description = "Учи удмуртские слова с яркими карточками — словарь разбит на интересные темы."
        ),
        OnboardingStep(
            emoji = "🎮",
            title = "Играй и запоминай",
            description = "Шесть мини-игр помогут закрепить слова: выбирай перевод, собирай слово, находи пару."
        ),
        OnboardingStep(
            emoji = "🌟",
            title = "Твой профиль",
            description = "Выбери аватар и имя, следи за своими успехами и ставь новые рекорды."
        ),
    )
}

/** Mascot image for each onboarding slide — every page uses a distinct, centered image. */
private fun mascotFor(kind: OnboardingKind, page: Int): DrawableResource {
    val mascots = when (kind) {
        OnboardingKind.APP -> listOf(
            Res.drawable.mascot_book,
            Res.drawable.mascot_reading,
            Res.drawable.mascot_think,
        )
        OnboardingKind.PARENT -> listOf(
            Res.drawable.mascot_think_left,
            Res.drawable.mascot_book_left,
            Res.drawable.mascot_reading_left,
        )
        OnboardingKind.CHILD -> listOf(
            Res.drawable.mascot_wave_closeup,
            Res.drawable.mascot_book_closeup,
            Res.drawable.mascot_think_closeup,
        )
    }
    return mascots[page.coerceIn(mascots.indices)]
}

/** Full-screen onboarding shown for the app, a parent account or a child account. */
@Composable
fun OnboardingPage(
    kind: OnboardingKind,
    onFinished: () -> Unit,
) {
    val steps = remember(kind) { stepsFor(kind) }
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLast = currentPage == steps.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Header (fixed above the pager)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ӟеч-а бур-а удмурт кыл?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = UdmurtRed
                )
            }
            TextButton(onClick = onFinished) {
                Text(
                    text = "Пропустить",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    fontSize = 13.sp
                )
            }
        }

        // Swipeable pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val step = steps[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Mascot illustration
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(UdmurtGold.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(mascotFor(kind, page)),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = step.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = step.description,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Bottom controls (fixed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            // Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .background(
                                color = if (index == currentPage) UdmurtRed
                                else UdmurtRed.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage - 1)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Назад", fontWeight = FontWeight.Bold)
                    }
                }
                T2Button(
                    text = if (isLast) "Начать" else "Далее",
                    onClick = {
                        if (isLast) {
                            onFinished()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    containerColor = UdmurtRed
                )
            }
        }
    }
}

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
    val title: String,
    val description: String,
)

private fun stepsFor(kind: OnboardingKind): List<OnboardingStep> = when (kind) {
    OnboardingKind.APP -> listOf(
        OnboardingStep(
            title = "Ӟечбуресь!",
            description = "Это приложение для детей от 5 до 10 лет. Здесь можно учить удмуртские слова, слушать их и играть."
        ),
        OnboardingStep(
            title = "Слова и карточки",
            description = "В словаре есть темы «Животные», «Еда», «Семья», «Цвета» и другие. На каждой карточке есть слово, перевод и звук."
        ),
        OnboardingStep(
            title = "Родитель и ребёнок",
            description = "Родитель создаёт профиль ребёнка. Ребёнок входит со своим логином и учится отдельно."
        ),
    )

    OnboardingKind.PARENT -> listOf(
        OnboardingStep(
            title = "Кабинет родителя",
            description = "Здесь видны профили детей, их прогресс и результаты игр."
        ),
        OnboardingStep(
            title = "Профили детей",
            description = "Добавьте ребёнка, укажите дату рождения и задайте логин и пароль для входа."
        ),
        OnboardingStep(
            title = "Отчёты",
            description = "В карточке ребёнка видно, сколько слов он выучил и какие слова вызывают ошибки. Отчёт можно скопировать для учителя."
        ),
    )

    OnboardingKind.CHILD -> listOf(
        OnboardingStep(
            title = "Привет!",
            description = "В словаре есть карточки со словами. Открой карточку, чтобы увидеть перевод и услышать слово."
        ),
        OnboardingStep(
            title = "Игры",
            description = "Шесть игр помогают запомнить слова. Выбирай перевод, собирай слова и ищи пары."
        ),
        OnboardingStep(
            title = "Профиль",
            description = "Выбери аватар. В профиле видно, сколько слов ты выучил и как играешь."
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ӟеч-а бур-а удмурт кыл?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onFinished) {
                Text(
                    text = "Пропустить",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            CircleShape
                        ),
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
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
                                color = if (index == currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                },
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
                        shape = CircleShape,
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Назад", style = MaterialTheme.typography.labelLarge)
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
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
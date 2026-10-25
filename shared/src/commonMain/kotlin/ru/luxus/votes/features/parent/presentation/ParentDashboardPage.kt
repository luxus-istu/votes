package ru.luxus.votes.features.parent.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.features.parent.domain.ChildProfileModel
import ru.luxus.votes.features.parent.domain.learningPaceLabel
import votes.shared.generated.resources.Res
import votes.shared.generated.resources.mascot_think

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardPage(
    viewModel: ParentViewModel,
    onLogout: () -> Unit,
    onAddChild: () -> Unit,
    onEditChild: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadChildren()
    }

    LaunchedEffect(state.notification) {
        val message = state.notification ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onEvent(ParentEvent.DismissNotification)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👨‍👩‍👧‍👦", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Кабинет родителя",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Статистика детей",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Выйти", color = UdmurtRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ParentDashboardContent(
                state = state,
                onSelectChild = { id -> viewModel.onEvent(ParentEvent.SelectChild(id)) },
                onOpenAdd = onAddChild,
                onOpenEdit = onEditChild,
                onPlayAudio = { u, t -> viewModel.onEvent(ParentEvent.PlayAudio(u, t)) },
                onCopyReport = { child ->
                    val report = viewModel.generateTeacherReport(child)
                    clipboardManager.setText(AnnotatedString(report))
                }
            )
        }
    }
}

@Composable
private fun ParentDashboardContent(
    state: ParentState,
    onSelectChild: (String) -> Unit,
    onOpenAdd: () -> Unit,
    onOpenEdit: (String) -> Unit,
    onPlayAudio: (String, String) -> Unit,
    onCopyReport: (ChildProfileModel) -> Unit
) {
    val selected = state.selectedChild
    var copyDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Summary Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = UdmurtRed)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Семейный дашборд",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Успеваемость детей по удмуртскому языку",
                    color = White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Детей",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${state.children.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Слов",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${state.totalLearnedAcrossAll}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = UdmurtGold
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Точность",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${(state.averageAccuracyAcrossAll * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: "Мои дети"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Мои дети:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (state.children.isNotEmpty()) {
                TextButton(onClick = onOpenAdd) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = UdmurtRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить", color = UdmurtRed, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.children.isEmpty()) {
            // No children yet: one prominent call-to-action block
            EmptyChildrenCard(onClick = onOpenAdd)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.children.forEach { child ->
                    ChildSummaryCard(
                        child = child,
                        isSelected = child.id == selected?.id,
                        onClick = { onSelectChild(child.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Detailed section for Selected Child
        if (selected != null) {
            Text(
                text = "Успехи и прогресс",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Child profile hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(UdmurtRed, UdmurtRed.copy(alpha = 0.88f), UdmurtGold)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(White.copy(alpha = 0.22f), CircleShape)
                                .border(2.dp, White.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selected.avatar, fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = listOf(selected.name, selected.lastName)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" "),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    append("${selected.age} лет")
                                    if (selected.birthDate.isNotBlank()) append(" • ${selected.birthDate}")
                                },
                                fontSize = 12.sp,
                                color = White.copy(alpha = 0.85f)
                            )
                            if (selected.childEmail.isNotBlank()) {
                                Text(
                                    text = "Вход: ${selected.childEmail}",
                                    fontSize = 11.sp,
                                    color = White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = { onOpenEdit(selected.id) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Редактировать профиль",
                                tint = White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroChip("🎈 ${learningPaceLabel(selected.targetWordsPerDay)} темп")
                        HeroChip("📚 ${selected.wordsLearnedCount} слов")
                        HeroChip("🎯 ${(selected.accuracy * 100).toInt()}%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Overall Course Mastery Progress Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Освоение курса:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        val pct = if (selected.totalWordsAvailable > 0) (selected.wordsLearnedCount * 100 / selected.totalWordsAvailable) else 0
                        Text(
                            text = "$pct%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = UdmurtRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progressFraction = if (selected.totalWordsAvailable > 0) selected.wordsLearnedCount.toFloat() / selected.totalWordsAvailable else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = UdmurtRed,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Выучено: ${selected.wordsLearnedCount} из ${selected.totalWordsAvailable} слов",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Точность: ${(selected.accuracy * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CorrectGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Topic Mastery Breakdown
            Text(
                text = "Темы словаря:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            val topics = selected.topicMastery.entries.take(6)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topics.forEach { (cat, pair) ->
                    TopicMasteryRow(category = cat, learned = pair.first, total = pair.second)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mini-Games Performance Matrix (6 games)
            Text(
                text = "Успеваемость в играх:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            val gameTitles = listOf(
                Triple("translate", "Выбери перевод", "🎯"),
                Triple("oddoneout", "Лишнее слово", "🔍"),
                Triple("sorting", "Сортировка", "📂"),
                Triple("memory", "Найди пару", "🃏"),
                Triple("compile", "Собери слово", "🔤"),
                Triple("truefalse", "Правда / Ложь", "⚖️")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                gameTitles.take(3).forEach { (key, title, emoji) ->
                    val pair = selected.gameStatsMap[key] ?: Pair(0, 0)
                    MiniGameStatTile(
                        emoji = emoji,
                        title = title,
                        answered = pair.first,
                        correct = pair.second,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                gameTitles.drop(3).take(3).forEach { (key, title, emoji) ->
                    val pair = selected.gameStatsMap[key] ?: Pair(0, 0)
                    MiniGameStatTile(
                        emoji = emoji,
                        title = title,
                        answered = pair.first,
                        correct = pair.second,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Problematic Words (Mistakes)
            Text(
                text = "Слова для повторения:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (selected.topErrors.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selected.topErrors.take(5).forEach { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = err.udmurt,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = err.russian,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = IncorrectRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${err.errorCount} ош.",
                                            color = IncorrectRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { onPlayAudio(err.udmurt, "[${err.udmurt}]") },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Озвучить",
                                            tint = UdmurtRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CorrectGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 Отличный результат! Частых ошибок не зафиксировано.",
                        modifier = Modifier.padding(16.dp),
                        color = CorrectGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pedagogical Advice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = UdmurtGold.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Совет педагога для родителей:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selected.accuracy >= 0.85f)
                                "${selected.name} отлично справляется с программой! Для углубления предложите составлять простые предложения из раздела «Фразы»."
                            else
                                "Рекомендуем уделить внимание игре «Собери слово» для закрепления букв (ӝ, ӟ, ӥ) и повторять слова из темы «Животные».",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Copy Report for School / Teacher Button
            T2Button(
                text = if (copyDone) "✓ Отчёт скопирован!" else "Скопировать отчёт для учителя",
                onClick = {
                    onCopyReport(selected)
                    copyDone = true
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (copyDone) CorrectGreen else UdmurtRed
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            color = White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ChildSummaryCard(
    child: ChildProfileModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) UdmurtRed.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(UdmurtGold.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(child.avatar, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listOf(child.name, child.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${child.age} лет • ${learningPaceLabel(child.targetWordsPerDay).lowercase()}",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = UdmurtRed.copy(alpha = 0.09f)
            ) {
                Text(
                    text = "${(child.accuracy * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = UdmurtRed,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text("✓", color = UdmurtRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun EmptyChildrenCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.5.dp, UdmurtRed.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = UdmurtRed.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 26.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.mascot_think),
                contentDescription = null,
                modifier = Modifier
                    .height(128.dp)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Добавить ребёнка",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Создайте профиль — здесь появится статистика занятий",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = UdmurtRed
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Создать профиль",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicMasteryRow(
    category: String,
    learned: Int,
    total: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                val fraction = if (total > 0) learned.toFloat() / total else 0f
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = UdmurtGold,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Text(
                text = "$learned / $total",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MiniGameStatTile(
    emoji: String,
    title: String,
    answered: Int,
    correct: Int,
    modifier: Modifier = Modifier
) {
    val accuracy = if (answered > 0) (correct * 100 / answered) else 0
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                maxLines = 2
            )
            Text(
                text = "$accuracy%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (accuracy >= 80) CorrectGreen else if (accuracy >= 60) UdmurtGold else IncorrectRed
            )
        }
    }
}

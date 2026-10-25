package ru.luxus.votes.features.parent.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.ageFromUtcMillis
import ru.luxus.votes.calculateAge
import ru.luxus.votes.core.theme.CorrectGreen
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.core.widgets.T2TextField
import ru.luxus.votes.currentYear
import ru.luxus.votes.dateDdMmYyyyToUtcMillis
import ru.luxus.votes.features.parent.domain.ChildProfileModel
import ru.luxus.votes.utcMillisToDdMmYyyy
import ru.luxus.votes.utcMillisYearsAgo

private val FORM_MASCOTS = listOf("🦊", "🐻", "🐰", "🐺", "🦔", "🐿️", "🦉", "🦆")

private data class PaceOption(
    val words: Int,
    val title: String,
    val wordsText: String,
    val hint: String,
)

private val PACE_OPTIONS = listOf(
    PaceOption(3, "Спокойный", "3 новых слова", "Короткие занятия без спешки"),
    PaceOption(5, "Обычный", "5 новых слов", "Рекомендуемый ритм для новичка"),
    PaceOption(10, "Интенсивный", "10 новых слов", "Для уверенных и увлечённых ребят"),
)

/**
 * Standalone page for creating a new child profile or editing an existing one.
 * Replaces the old modal dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildFormPage(
    childId: String?,
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditing = childId != null
    val existing: ChildProfileModel? = remember(childId, state.children) {
        state.children.firstOrNull { it.id == childId }
    }

    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var lastName by remember(existing?.id) { mutableStateOf(existing?.lastName.orEmpty()) }
    var birthDate by remember(existing?.id) { mutableStateOf(existing?.birthDate.orEmpty()) }
    var avatar by remember(existing?.id) { mutableStateOf(existing?.avatar ?: FORM_MASCOTS.first()) }
    var pace by remember(existing?.id) { mutableStateOf(existing?.targetWordsPerDay ?: 5) }
    var childEmail by remember(existing?.id) { mutableStateOf(existing?.childEmail.orEmpty()) }
    var childPassword by remember(existing?.id) { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val age = calculateAge(birthDate)

    LaunchedEffect(state.formSaved) {
        if (state.formSaved) {
            viewModel.onEvent(ParentEvent.ConsumeFormSaved)
            onSaved()
        }
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
                    Text(
                        text = if (isEditing) "Профиль ребёнка" else "Новый ребёнок",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Hero header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(UdmurtGold.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatar, fontSize = 34.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isEditing) {
                            listOf(name, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Ребёнок" }
                        } else "Знакомство",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isEditing)
                            "Обновите данные профиля и сохраните изменения"
                        else
                            "Заполните профиль — это займёт меньше минуты",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Basic info card
            FormCard(title = "Основное", emoji = "📝") {
                T2TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Имя ребёнка"
                )

                Spacer(modifier = Modifier.height(12.dp))

                T2TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Фамилия (необязательно)"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    DateField(
                        value = birthDate,
                        modifier = Modifier.weight(1f),
                        onClick = { showDatePicker = true }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    if (age != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CorrectGreen.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "$age лет",
                                color = CorrectGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Талисман:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FORM_MASCOTS.forEach { mascot ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (avatar == mascot) UdmurtGold.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (avatar == mascot) 2.dp else 0.dp,
                                    color = if (avatar == mascot) UdmurtGold else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .clickable { avatar = mascot },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mascot, fontSize = 22.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Learning pace card
            FormCard(title = "Темп занятий", emoji = "🎈") {
                PACE_OPTIONS.forEachIndexed { index, option ->
                    val selected = pace == option.words
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) UdmurtRed.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) UdmurtRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { pace = option.words }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selected) UdmurtRed else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${option.wordsText} • ${option.hint}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                        if (selected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = UdmurtRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (index != PACE_OPTIONS.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login credentials card
            FormCard(title = "Вход ребёнка", emoji = "🔑") {
                T2TextField(
                    value = childEmail,
                    onValueChange = { childEmail = it },
                    label = "Email или логин ребёнка"
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = childPassword,
                    onValueChange = { childPassword = it },
                    label = if (isEditing) "Новый пароль (пусто — не менять)" else "Пароль для входа",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "С этими данными ребёнок входит на экране входа.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IncorrectRed.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = IncorrectRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            T2Button(
                text = if (isEditing) "Сохранить изменения" else "Создать профиль",
                onClick = {
                    val validationError = validateForm(
                        name = name,
                        age = age,
                        childEmail = childEmail,
                        childPassword = childPassword,
                        isEditing = isEditing,
                    )
                    if (validationError != null) {
                        errorMessage = validationError
                    } else {
                        errorMessage = null
                        if (childId != null) {
                            viewModel.onEvent(
                                ParentEvent.UpdateChild(
                                    id = childId,
                                    name = name.trim(),
                                    lastName = lastName.trim(),
                                    birthDate = birthDate,
                                    avatar = avatar,
                                    targetWords = pace,
                                    childEmail = childEmail.trim(),
                                    childPassword = childPassword,
                                )
                            )
                        } else {
                            viewModel.onEvent(
                                ParentEvent.AddChild(
                                    name = name.trim(),
                                    lastName = lastName.trim(),
                                    birthDate = birthDate,
                                    avatar = avatar,
                                    targetWords = pace,
                                    childEmail = childEmail.trim(),
                                    childPassword = childPassword,
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = UdmurtRed
            )

            if (childId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IncorrectRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Удалить профиль ребёнка")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val todayYear = currentYear() ?: 2026
        val yearRange = (todayYear - 11)..todayYear
        val initialMillis = dateDdMmYyyyToUtcMillis(birthDate)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis ?: utcMillisYearsAgo(7),
            yearRange = yearRange,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val selectedAge = ageFromUtcMillis(utcTimeMillis) ?: return false
                    return selectedAge in 5..10
                }

                override fun isSelectableYear(year: Int): Boolean = year in yearRange
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthDate = utcMillisToDdMmYyyy(millis)
                        }
                        errorMessage = null
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Готово", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && childId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить профиль?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Профиль, статистика и прогресс ребёнка будут удалены без возможности восстановления.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.onEvent(ParentEvent.DeleteChild(childId))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IncorrectRed)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun validateForm(
    name: String,
    age: Int?,
    childEmail: String,
    childPassword: String,
    isEditing: Boolean,
): String? = when {
    name.isBlank() -> "Введите имя ребёнка"
    age == null -> "Укажите дату рождения ребёнка"
    age !in 5..10 -> "Приложение рассчитано на детей 5–10 лет"
    childEmail.isBlank() -> "Укажите email или логин ребёнка"
    !isEditing && childPassword.isBlank() -> "Придумайте пароль для входа ребёнка"
    childPassword.isNotBlank() && childPassword.length < 4 -> "Пароль должен быть не короче 4 символов"
    else -> null
}

@Composable
private fun DateField(
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Дата рождения",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = value.ifBlank { "Выберите дату" },
                    fontSize = 16.sp,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Открыть календарь",
                tint = UdmurtRed
            )
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    emoji: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$emoji $title",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

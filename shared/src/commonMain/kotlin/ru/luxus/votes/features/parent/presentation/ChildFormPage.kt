package ru.luxus.votes.features.parent.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.luxus.votes.core.datetime.ageFromUtcMillis
import ru.luxus.votes.core.datetime.calculateAge
import ru.luxus.votes.core.datetime.currentYear
import ru.luxus.votes.core.datetime.dateDdMmYyyyToUtcMillis
import ru.luxus.votes.core.datetime.utcMillisToDdMmYyyy
import ru.luxus.votes.core.datetime.utcMillisYearsAgo
import ru.luxus.votes.core.theme.LocalExtendedColors
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.core.widgets.T2TextField
import ru.luxus.votes.features.parent.domain.ChildProfileModel

private val FORM_MASCOTS = listOf("🦊", "🐻", "🐰", "🐺", "🦔", "🐿️", "🦉", "🦆")

private data class PaceOption(
    val words: Int,
    val title: String,
    val wordsText: String,
)

private val PACE_OPTIONS = listOf(
    PaceOption(3, "Спокойный", "3 новых слова в день"),
    PaceOption(5, "Обычный", "5 новых слов в день"),
    PaceOption(10, "Интенсивный", "10 новых слов в день"),
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val extended = LocalExtendedColors.current

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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
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
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatar,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isEditing) {
                                listOf(name, lastName).filter { it.isNotBlank() }
                                    .joinToString(" ").ifBlank { "Ребёнок" }
                            } else "О ребёнке",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isEditing)
                                "Измените данные и сохраните"
                            else
                                "Имя, дата рождения и темп занятий.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Basic info card
                FormCard(title = "Данные ребёнка") {
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
                                shape = MaterialTheme.shapes.small,
                                color = extended.successContainer
                            ) {
                                Text(
                                    text = "$age лет",
                                    color = extended.onSuccessContainer,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Талисман",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FORM_MASCOTS.chunked(4).forEach { rowMascots ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowMascots.forEach { mascot ->
                                    val selected = avatar == mascot
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                            .border(
                                                width = if (selected) 2.dp else 0.dp,
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = CircleShape
                                            )
                                            .selectable(
                                                selected = selected,
                                                onClick = { avatar = mascot },
                                                role = Role.RadioButton
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mascot,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Learning pace card
                FormCard(title = "Темп занятий") {
                    PACE_OPTIONS.forEachIndexed { index, option ->
                        val selected = pace == option.words
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    }
                                )
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = MaterialTheme.shapes.medium
                                )
                                .selectable(
                                    selected = selected,
                                    onClick = { pace = option.words },
                                    role = Role.RadioButton
                                )
                                .heightIn(min = 56.dp)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = option.wordsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
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
                FormCard(title = "Вход ребёнка") {
                    T2TextField(
                        value = childEmail,
                        onValueChange = { childEmail = it },
                        label = "Email или логин ребёнка"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    T2TextField(
                        value = childPassword,
                        onValueChange = { childPassword = it },
                        label = if (isEditing) "Новый пароль (оставьте пустым, чтобы не менять)" else "Пароль для входа",
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Эти данные ребёнок вводит на экране входа.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
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
                    modifier = Modifier.fillMaxWidth()
                )

                if (childId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Удалить профиль ребёнка",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
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
            title = { Text("Удалить профиль ребёнка?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Профиль, статистика и прогресс ребёнка удалятся навсегда.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.onEvent(ParentEvent.DeleteChild(childId))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
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
    age !in 5..10 -> "Приложение рассчитано на детей от 5 до 10 лет"
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
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.ifBlank { "Выберите дату" },
                    style = MaterialTheme.typography.bodyLarge,
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
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

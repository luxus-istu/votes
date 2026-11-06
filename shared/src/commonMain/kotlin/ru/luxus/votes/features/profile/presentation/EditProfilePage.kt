package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.core.widgets.T2TextField
import ru.luxus.votes.features.auth.presentation.AuthEvent
import ru.luxus.votes.features.auth.presentation.AuthState
import ru.luxus.votes.features.auth.presentation.AuthViewModel

@Composable
fun EditProfilePage(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val user = (authState as? AuthState.Authenticated)?.user

    var firstName by remember(user) { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember(user) { mutableStateOf(user?.lastName ?: "") }
    var avatar by remember(user) { mutableStateOf(user?.avatar ?: "🦊") }

    val avatars = listOf("🦊", "🐻", "🐰", "🐿️", "🦔", "🐎", "🦉", "🌸")

    QuizScaffold(
        title = "Редактировать профиль",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Талисман",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            avatars.chunked(4).forEachIndexed { index, rowAvatars ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowAvatars.forEach { av ->
                        AvatarItem(
                            avatar = av,
                            isSelected = avatar == av,
                            onSelect = { avatar = av },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            T2TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "Имя"
            )

            Spacer(modifier = Modifier.height(14.dp))

            T2TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Фамилия"
            )

            Spacer(modifier = Modifier.height(20.dp))

            T2Button(
                text = "Сохранить изменения",
                onClick = {
                    authViewModel.onEvent(
                        AuthEvent.UpdateProfile(
                            firstName = firstName,
                            lastName = lastName,
                            age = user?.age ?: 7,
                            role = user?.role ?: "Ученик",
                            avatar = avatar
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AvatarItem(
    avatar: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 64.dp)
            .aspectRatio(1f)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = CircleShape
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatar,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
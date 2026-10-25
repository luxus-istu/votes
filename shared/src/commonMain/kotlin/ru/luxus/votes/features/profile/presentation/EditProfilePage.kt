package ru.luxus.votes.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
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
    val authState by authViewModel.state.collectAsState()
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
            // Avatar picker
            Text(
                text = "Выбери аватар-талисман:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                avatars.take(4).forEach { av ->
                    AvatarItem(
                        avatar = av,
                        isSelected = avatar == av,
                        onSelect = { avatar = av }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                avatars.drop(4).forEach { av ->
                    AvatarItem(
                        avatar = av,
                        isSelected = avatar == av,
                        onSelect = { avatar = av }
                    )
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
                modifier = Modifier.fillMaxWidth(),
                containerColor = UdmurtRed
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AvatarItem(
    avatar: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) UdmurtGold.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onSelect)
            .then(
                if (isSelected) Modifier.border(3.dp, UdmurtRed, CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(avatar, fontSize = 32.sp)
    }
}

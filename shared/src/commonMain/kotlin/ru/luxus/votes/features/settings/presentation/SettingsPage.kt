package ru.luxus.votes.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.Magenta
import ru.luxus.votes.core.widgets.QuizScaffold

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    QuizScaffold(
        title = "Настройки",
        onBack = onBack
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitchRow(
                    title = "Звуковые эффекты",
                    description = "Звуки при правильных ответах",
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleSound(it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SettingSwitchRow(
                    title = "Уведомления",
                    description = "Напоминания о ежедневных уроках",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleNotifications(it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SettingSwitchRow(
                    title = "Геолокация",
                    description = "Уведомления о культурных местах рядом",
                    checked = settings.geolocationEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleGeolocation(it)) }
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Magenta, checkedTrackColor = Magenta.copy(alpha = 0.4f))
        )
    }
}

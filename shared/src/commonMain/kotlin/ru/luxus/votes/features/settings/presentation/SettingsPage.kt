package ru.luxus.votes.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.IncorrectRed
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.widgets.QuizScaffold
import ru.luxus.votes.core.widgets.T2Button

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Параметры приложения:",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SettingSwitchRow(
                        title = "Звуковые эффекты и озвучка",
                        description = "Произношение слов и звуки в викторинах",
                        checked = settings.soundEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleSound(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingSwitchRow(
                        title = "Уведомления",
                        description = "Напоминания о новых словах дня и уроках",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleNotifications(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingSwitchRow(
                        title = "Геолокация",
                        description = "Уведомления о культурных памятниках Удмуртии рядом",
                        checked = settings.geolocationEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleGeolocation(it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App & State Program info
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
                            text = "О программе",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Приложение «Ӟеч-а бур-а удмурт кыл?» (Версия 1.0.0, Offline Edition).\n" +
                                "Разработано в рамках государственной программы Удмуртской Республики «Сохранение, изучение и развитие удмуртского языка» (Постановление Правительства УР № 748 от 20.11.2023 г.).\n\n" +
                                "Все материалы, карточки, озвучка и игры полностью автономны и не требуют подключения к сети интернет.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                fontSize = 15.sp,
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
            colors = SwitchDefaults.colors(checkedThumbColor = UdmurtRed, checkedTrackColor = UdmurtRed.copy(alpha = 0.35f))
        )
    }
}

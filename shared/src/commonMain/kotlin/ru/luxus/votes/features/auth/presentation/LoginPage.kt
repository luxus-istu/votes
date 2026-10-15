package ru.luxus.votes.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.Magenta
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.core.widgets.T2TextField
import ru.luxus.votes.core.widgets.T2TopSnackBar

@Composable
fun LoginPage(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var isRegisterTab by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Мужской") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        when (val current = state) {
            is AuthState.Authenticated -> onLoginSuccess()
            is AuthState.Error -> {
                errorMessage = current.message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Удмурт кыл",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Magenta,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isRegisterTab) "Создайте аккаунт" else "Добро пожаловать обратно",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Switch Tab buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(100.dp)
                    )
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isRegisterTab = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isRegisterTab) Magenta else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isRegisterTab) White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Вход", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isRegisterTab = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegisterTab) Magenta else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isRegisterTab) White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Регистрация", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isRegisterTab) {
                T2TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Имя"
                )
                Spacer(modifier = Modifier.height(12.dp))

                T2TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Фамилия"
                )
                Spacer(modifier = Modifier.height(12.dp))

                T2TextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = "Пол"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            T2TextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )
            Spacer(modifier = Modifier.height(12.dp))

            T2TextField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = Magenta)
            } else {
                T2Button(
                    text = if (isRegisterTab) "Зарегистрироваться" else "Войти",
                    onClick = {
                        errorMessage = null
                        if (isRegisterTab) {
                            viewModel.onEvent(
                                AuthEvent.SubmitRegister(
                                    email = email,
                                    password = password,
                                    firstName = firstName,
                                    lastName = lastName,
                                    gender = gender
                                )
                            )
                        } else {
                            viewModel.onEvent(AuthEvent.SubmitLogin(email, password))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        viewModel.onEvent(AuthEvent.SubmitAnonymous)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Войти как гость", fontWeight = FontWeight.Bold, color = Magenta)
                }
            }
        }

        T2TopSnackBar(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

package ru.luxus.votes.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import org.jetbrains.compose.resources.painterResource
import ru.luxus.votes.core.theme.UdmurtRed
import ru.luxus.votes.core.theme.White
import ru.luxus.votes.core.widgets.T2Button
import ru.luxus.votes.core.widgets.T2TextField
import ru.luxus.votes.core.widgets.T2TopSnackBar
import votes.shared.generated.resources.Res
import votes.shared.generated.resources.mascot_book

@Composable
fun LoginPage(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // "login" | "register"
    var mode by remember { mutableStateOf("login") }

    // Login (parent or child: email + password)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Parent registration
    var regFirstName by remember { mutableStateOf("") }
    var regLastName by remember { mutableStateOf("") }

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
            .windowInsetsPadding(WindowInsets.safeDrawing)
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
            Image(
                painter = painterResource(Res.drawable.mascot_book),
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Ӟеч-а бур-а удмурт кыл?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = UdmurtRed,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Здравствуй, удмуртский язык!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Switch: Вход / Регистрация
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(100.dp)
                    )
                    .padding(4.dp)
            ) {
                SegmentButton(
                    text = "Вход",
                    selected = mode == "login",
                    modifier = Modifier.weight(1f),
                    onClick = { mode = "login" }
                )
                SegmentButton(
                    text = "Регистрация",
                    selected = mode == "register",
                    modifier = Modifier.weight(1f),
                    onClick = { mode = "register" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (mode == "register") {
                Text(
                    text = "Регистрация родителя",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                T2TextField(
                    value = regFirstName,
                    onValueChange = { regFirstName = it },
                    label = "Имя"
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = regLastName,
                    onValueChange = { regLastName = it },
                    label = "Фамилия"
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email или логин"
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "После регистрации добавьте профили детей в кабинете родителя — там же вы зададите email и пароль для входа ребёнка.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {
                T2TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email или логин"
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Подходит для входа родителя и ребёнка (логин ребёнка выдаётся в кабинете родителя).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = UdmurtRed)
            } else {
                T2Button(
                    text = if (mode == "register") "Зарегистрироваться" else "Войти",
                    onClick = {
                        errorMessage = null
                        if (mode == "register") {
                            viewModel.onEvent(
                                AuthEvent.SubmitRegisterParent(
                                    email = email,
                                    password = password,
                                    firstName = regFirstName,
                                    lastName = regLastName
                                )
                            )
                        } else {
                            viewModel.onEvent(AuthEvent.SubmitLogin(email, password))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = UdmurtRed
                )
            }
        }

        T2TopSnackBar(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = UdmurtRed
        )
    }
}

@Composable
private fun SegmentButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) UdmurtRed else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) White else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

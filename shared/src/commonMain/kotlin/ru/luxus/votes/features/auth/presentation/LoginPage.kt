package ru.luxus.votes.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
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
    val state by viewModel.state.collectAsStateWithLifecycle()

    // "login" | "register"
    var mode by rememberSaveable { mutableStateOf("login") }

    // Login (parent or child: email + password)
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // Parent registration
    var regFirstName by rememberSaveable { mutableStateOf("") }
    var regLastName by rememberSaveable { mutableStateOf("") }

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
            .imePadding()
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Здравствуй, удмуртский язык!",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                T2TextField(
                    value = regFirstName,
                    onValueChange = { regFirstName = it },
                    label = "Имя",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = regLastName,
                    onValueChange = { regLastName = it },
                    label = "Фамилия",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email или логин",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "После регистрации добавьте профиль ребёнка и придумайте для него логин и пароль.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                T2TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email или логин",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                T2TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Этот экран подходит для входа родителя и ребёнка. Логин ребёнка создаётся в кабинете родителя.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        T2TopSnackBar(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
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
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        shape = RoundedCornerShape(100.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
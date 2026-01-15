package com.example.pharmacystore.ui.auth.signIn

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.ui.theme.sagePerSecond

@Composable
fun EmailPasswordSignIn(
    emailPasswordSignInViewModel: EmailPasswordSignInViewModel,
    // navigateToHomeScreen: () -> Unit,
    navigateToAuthGate: () -> Unit,
    navigateToSingInMethodScreen: () -> Unit,
    navigateToSignInMfaCode: () -> Unit
) {

    val context = LocalContext.current
    val activity = context as Activity
    var password by remember { mutableStateOf("") }

    val email by emailPasswordSignInViewModel.email.collectAsState()
    val authUiState by emailPasswordSignInViewModel.authUiState.collectAsState()
    val validation by emailPasswordSignInViewModel.validationState.collectAsState()

    val isLoading = authUiState is EmailPasswordSignInViewModel.AuthUiState.Loading

    DoubleBackReact(
        exit = { navigateToSingInMethodScreen() },
        message = "Press back again to return to starting screen"
    )

    LaunchedEffect(emailPasswordSignInViewModel.events) {
        emailPasswordSignInViewModel.events.collect { data ->
            when (data) {
                is EmailPasswordSignInViewModel.AuthEvent.Error -> {
                    password = ""
                }

                EmailPasswordSignInViewModel.AuthEvent.NavigateToMainScreen -> {
                    navigateToAuthGate()
                    Toast.makeText(context, "Logged In", Toast.LENGTH_SHORT).show()
                }

                EmailPasswordSignInViewModel.AuthEvent.NavigateToMfaSmsCodeScreen -> {
                    navigateToSignInMfaCode()
                }
            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            // emailPasswordSignUpViewModel.resetAuthState()
            emailPasswordSignInViewModel.updateEmail("") // reset emaila
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(returnGradientBackGround())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AuthHeaderCard(
                title = "Sign In",
                subtitle = "Log in with your e-mail and password.",
                icon = Icons.AutoMirrored.Filled.Login
            )

            if (isLoading) {
                AuthSectionCard(
                    title = "Please wait",
                    subtitle = "We are signing you in."
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            } else {
                AuthSectionCard(
                    title = "Credentials",
                    subtitle = "Enter your account details."
                ) {
                    val ink = authInk()
                    val tfColors = authOutlinedTextFieldColors()

                    OutlinedTextField(
                        value = email,
                        onValueChange = { emailPasswordSignInViewModel.updateEmail(it) },
                        label = { Text("E-mail") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = email.isNotEmpty() && !validation.email,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = null,
                                tint = sagePerSecond
                            )
                        }
                    )

                    if (email.isNotEmpty() && !validation.email) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Invalid email",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    var pwdVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            emailPasswordSignInViewModel.validatePassword(it)
                        },
                        label = { Text("password") },
                        singleLine = true,
                        visualTransformation =
                            if (pwdVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (pwdVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff
                            IconButton(onClick = { pwdVisible = !pwdVisible }) {
                                Icon(icon, contentDescription = null, tint = sagePerSecond)
                            }
                        },
                        isError = password.isNotEmpty() && !validation.password,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = sagePerSecond)
                        }
                    )

                    when (val s = authUiState) {
                        is EmailPasswordSignInViewModel.AuthUiState.Error -> {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 12.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = s.error,
                                        color = ink,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }


                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = {
                            emailPasswordSignInViewModel.signIn(email, password, activity)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = validation.email && email.isNotEmpty()
                                && password.isNotEmpty() && validation.password,
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 1.dp,
                            hoveredElevation = 8.dp,
                            focusedElevation = 8.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text("Sign In")
                    }
                }
            }

            Spacer(Modifier.height(90.dp))
        }
    }
}

/* ─────────────────────────  UI wspólne (tylko wygląd)  ───────────────────────── */

@Composable
fun authInk(): Color = Color(0xFF1B1B12)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun authOutlinedTextFieldColors(): TextFieldColors {
    val ink = authInk()
    val primary = MaterialTheme.colorScheme.primary

    val container = Color.White.copy(alpha = 0.15f)


    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = ink,
        unfocusedTextColor = ink,
        cursorColor = primary,

        focusedLabelColor = ink.copy(alpha = 0.80f),
        unfocusedLabelColor = ink.copy(alpha = 0.70f),

        focusedContainerColor = container,
        unfocusedContainerColor = container,

        focusedBorderColor = primary.copy(alpha = 0.75f),
        unfocusedBorderColor = ink.copy(alpha = 0.22f),

        focusedLeadingIconColor = sagePerSecond,
        unfocusedLeadingIconColor = sagePerSecond,
        focusedTrailingIconColor = sagePerSecond,
        unfocusedTrailingIconColor = sagePerSecond,
    )
}

@Composable
fun AuthHeaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    val ink = authInk()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(returnGradientBackGround())
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.dp, sagePerSecond.copy(alpha = 0.25f), CircleShape)
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = sagePerSecond)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ink
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ink.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
fun AuthSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val ink = authInk()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(returnGradientBackGround())
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ink
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ink.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                content()
            }
        }
    }
}
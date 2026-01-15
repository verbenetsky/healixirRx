package com.example.pharmacystore.ui.auth.linkEmail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.domain.model.Validation
import com.example.pharmacystore.ui.auth.InfoDialog
import com.example.pharmacystore.ui.auth.signIn.AuthHeaderCard
import com.example.pharmacystore.ui.auth.signIn.AuthSectionCard
import com.example.pharmacystore.ui.auth.signIn.authInk
import com.example.pharmacystore.ui.auth.signIn.authOutlinedTextFieldColors
import com.example.pharmacystore.ui.theme.sagePerSecond
import kotlinx.coroutines.flow.SharedFlow

// taki sam screen jak EmailPasswordSignUp tylko ze troche inny use case

@Composable
fun LinkEmailPasswordScreen(
    linkEmailEvent: SharedFlow<LinkEmailViewModel.LinkEmailEvent>,
    linkEmailState: LinkEmailViewModel.LinkEmailState,
    email: String,
    validation: Validation,
    updateEmail: (String) -> Unit,
    navigateToProfileScreen: () -> Unit,
    validatePassword: (String) -> Unit,
    linkEmail: (String, String) -> Unit
) {
    var dialogText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var password1 by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }

    val isLoading = linkEmailState is LinkEmailViewModel.LinkEmailState.Loading

    LaunchedEffect(Unit) {
        linkEmailEvent.collect { value ->
            when (value) {
                is LinkEmailViewModel.LinkEmailEvent.EmailSuccessfullyLinked -> {
                    navigateToProfileScreen()
                }
            }
        }
    }

    DoubleBackReact(
        message = "Press back again to return to home screen",
        exit = { navigateToProfileScreen() }
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(returnGradientBackGround())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AuthHeaderCard(
                title = "Link email to your account",
                subtitle = "Add email and password sign-in to your existing account.",
                icon = Icons.Outlined.Link
            )

            if (isLoading) {
                AuthSectionCard(
                    title = "Please wait",
                    subtitle = "We are linking your account."
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
                    subtitle = "Enter your e-mail and choose a strong password."
                ) {
                    val ink = authInk()
                    val tfColors = authOutlinedTextFieldColors()

                    OutlinedTextField(
                        value = email,
                        onValueChange = { updateEmail(it) },
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
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    var pwdVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password1,
                        onValueChange = {
                            password1 = it
                            validatePassword(it)
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
                        isError = password1.isNotEmpty() && !validation.password,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = sagePerSecond)
                        }
                    )
                    if (password1.isNotEmpty() && !validation.password) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                0.6.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Min. 8 chars, at least one capital and small letter, one digit and one special char",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF000000)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password2,
                        onValueChange = { password2 = it },
                        label = { Text("Repeat password") },
                        singleLine = true,
                        visualTransformation =
                            if (pwdVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        isError = password2.isNotEmpty() && password2 != password1,
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.LockReset,
                                contentDescription = null,
                                tint = sagePerSecond
                            )
                        }
                    )
                    if (password2.isNotEmpty() && password2 != password1) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Passwords don't match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    when (linkEmailState) {
                        is LinkEmailViewModel.LinkEmailState.Error -> {
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
                                        text = linkEmailState.error,
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
                            linkEmail(email, password1)
                            password1 = ""
                            password2 = ""
                        },

                        enabled = validation.email &&
                                validation.password &&
                                password1 == password2 &&
                                password1.isNotEmpty() &&
                                password2.isNotEmpty() &&
                                email.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 1.dp,
                            hoveredElevation = 8.dp,
                            focusedElevation = 8.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text("Sign Up")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.ime.union(WindowInsets.navigationBars)
                )
            )
        }

        if (showDialog) {
            InfoDialog(
                text = dialogText,
                onDismiss = {
                    showDialog = false
                    navigateToProfileScreen()
                }
            )
        }
    }
}
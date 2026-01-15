package com.example.pharmacystore.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.domain.model.UserSettings
import com.example.pharmacystore.ui.profileScreen.ElevatedActionChip
import com.example.pharmacystore.ui.profileSetUp.profileSetupInk
import com.example.pharmacystore.ui.settings.SettingsViewModel.TwoFaEvents
import com.example.pharmacystore.ui.theme.sagePerSecond
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun Settings(
    twoFaState: SettingsViewModel.TwoFaState,
    twoFaEvents: SharedFlow<TwoFaEvents>,

    isVerified: Boolean,
    twoFAState: Boolean,
    enableLinkingState: Boolean,
    reAuthState: SettingsViewModel.ReAuthState,
    reAuthUser: (String, Activity) -> Unit,
    onEnableLinkingChange: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel,
    userSettings: UserSettings?,
    refreshUser: () -> Unit,
    cue: String?,
    isDirty: Boolean,

    resetStateToIdle: () -> Unit,
    navigateToCodeScreen: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity

    val haptics = LocalHapticFeedback.current
    var pulsing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pulsing) 1.05f else 1f, label = "scale")


    var twoFADialog by remember { mutableStateOf(false) }
    var providePasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        twoFaEvents.collect { value ->
            when (value) {
                TwoFaEvents.ClearPassword -> {
                    password = ""
                }

                is TwoFaEvents.NavigateToCodeScreen -> {
                    providePasswordDialog = false
                    navigateToCodeScreen()
                    password = ""
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.SettingsScreenEvent.ShowToast -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                }

                is SettingsViewModel.SettingsScreenEvent.ShowToastRefresh -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                    refreshUser()
                }
            }
        }
    }

    var cueConsumed by rememberSaveable(key = cue ?: "none") {
        mutableStateOf(cue == null)
    }

    LaunchedEffect(cueConsumed) {
        if (!cueConsumed) {
            delay(50)
            repeat(5) {
                pulsing = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(260)
                pulsing = false
                delay(220)
            }
            cueConsumed = true
        }
    }


    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // lewy prostokąt (tekst)
            Surface(
                modifier = Modifier
                    .weight(1f),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)

            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Security, null, tint = sagePerSecond)
                    Column {
                        Text(
                            "Two-factor authentication (2FA)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Enhances account security at sign-in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // prawy „prostokąt”: sam switch
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
            ) {
                Box(
                    Modifier
                        .padding(horizontal = 12.dp)
                        .heightIn(min = 56.dp)
                        .defaultMinSize(minWidth = 84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Switch(
                        enabled = !twoFAState,
                        checked = twoFAState,
                        onCheckedChange = {
                            settingsViewModel.checkIfEmailIsVerified { twoFADialog = true }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // lewy prostokąt (tekst)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)

            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Link, null, tint = sagePerSecond)
                    Column {
                        Text(
                            "Enable account linking",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "You can now log in to your account using either email and password or your phone number.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // prawy „prostokąt”: sam switch
            Surface(
                Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
            ) {
                Box(
                    Modifier
                        .padding(horizontal = 12.dp)
                        .heightIn(min = 56.dp)
                        .defaultMinSize(minWidth = 84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Switch(
                        checked = enableLinkingState,
                        onCheckedChange = { onEnableLinkingChange(it) }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = isDirty,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ),
            exit = fadeOut(animationSpec = tween(500)) +
                    scaleOut(
                        targetScale = 0.9f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )
        ) {
            if (userSettings != null) {
                ElevatedActionChip(
                    label = "Save",
                    onClick = { settingsViewModel.saveSettings(userSettings) })
            }
        }


//        AssistChip(
//            onClick = { /* no-op */ },
//            label = { Text(if (true) "2FA enabled" else "2FA disabled") },
//            colors = AssistChipDefaults.assistChipColors(
//                labelColor = if (true) sagePerSecond else MaterialTheme.colorScheme.onSurfaceVariant
//            ),
//            enabled = false
//        )

        if (twoFADialog) {
            TwoFADialog(
                onConfirm = {
                    providePasswordDialog = true
                    resetStateToIdle()
                    twoFADialog = false
                    //on2FAChange(true)
                },
                isVerified = isVerified,
                onDismiss = { twoFADialog = false }
            )
        }

        if (providePasswordDialog) {
            ProvidePasswordDialog(
                state = reAuthState,
                password = password,
                onPasswordChange = { newValue -> password = newValue },
                onConfirm = { password ->
                    reAuthUser(password, activity)
                    twoFADialog = false
                },
                twoFAState = twoFaState,
                onDismiss = { providePasswordDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFADialog(
    onConfirm: () -> Unit,
    isVerified: Boolean,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isVerified) {
                        "Enable Two-Factor Authentication (2FA)?"
                    } else {
                        "Email verification required"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                if (isVerified) {
                    Text(
                        text = "After enabling 2FA, every sign-in will require your password and a verification code sent via SMS to your phone number.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Warning: this action cannot be undone in the app. If you confirm, 2FA will be required for every login from now on.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "You can enable 2FA only after verifying your email address. Please check your inbox (and spam folder) and confirm your email, then try again.\n\n" +
                                "Additionally, 2FA requires an email and password to be linked to your account. If your account doesn’t have an email/password sign-in method set up yet, please add (link) an email and password first, then try again.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isVerified) {
                        TextButton(
                            onClick = { onDismiss() }
                        ) { Text("Cancel") }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            if (isVerified) onConfirm() else onDismiss()
                        }
                    ) {
                        Text(if (isVerified) "I understand" else "OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidePasswordDialog(
    twoFAState: SettingsViewModel.TwoFaState,
    password: String,
    onPasswordChange: (String) -> Unit,
    state: SettingsViewModel.ReAuthState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pwdVisible by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Confirm your password",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "For your security, we need you to re-enter your password before continuing. " +
                            "This helps protect your account when changing sensitive settings, such as enabling two-factor authentication.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (twoFAState) {
                    SettingsViewModel.TwoFaState.CheckingPassword -> {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { onPasswordChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            placeholder = {
                                Text(
                                    "Password",
                                    color = profileSetupInk().copy(alpha = 0.7f)
                                )
                            },
                            visualTransformation =
                                if (pwdVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = sagePerSecond
                                )
                            },
                            isError = state is SettingsViewModel.ReAuthState.Error,
                            trailingIcon = {
                                val icon = if (pwdVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff
                                IconButton(onClick = { pwdVisible = !pwdVisible }) {
                                    Icon(icon, contentDescription = null, tint = sagePerSecond)
                                }
                            }
                        )
                    }
                }

                when (state) {
                    is SettingsViewModel.ReAuthState.Error -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = state.msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                            )
                        }
                    }

                    else -> Unit
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        enabled = password.isNotBlank(),
                        onClick = { onConfirm(password) }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}




package com.example.pharmacystore.ui.settings

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.domain.model.UserInformationModel
import com.example.pharmacystore.domain.model.UserSettings
import com.example.pharmacystore.ui.profileScreen.ElevatedActionChip
import com.example.pharmacystore.ui.profileScreen.ProfileScreenViewModel
import com.example.pharmacystore.ui.theme.sagePerSecond
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
fun Settings(
    twoFAState: Boolean,
    on2FAChange: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel,
    userSettings: UserSettings?,
    refreshUser: () -> Unit,
    cue: String?,
    isDirty: Boolean,
) {
    val context = LocalContext.current

    val state by settingsViewModel.state.collectAsState()

    val haptics = LocalHapticFeedback.current

    var pulsing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pulsing) 1.05f else 1f, label = "scale")


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
                        checked = twoFAState,
                        onCheckedChange = { on2FAChange(it) }
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
    }
}


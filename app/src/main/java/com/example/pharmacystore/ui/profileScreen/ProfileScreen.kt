package com.example.pharmacystore.ui.profileScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.combineAddress
import com.example.pharmacystore.common.convertWojNumberToWojString
import com.example.pharmacystore.ui.auth.signIn.EmailPasswordSignInViewModel
import com.example.pharmacystore.ui.auth.signIn.EmailVerificationViewModel
import com.example.pharmacystore.ui.theme.sagePerSecond
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ProfileScreen(
    emailVerifiedState: EmailVerificationViewModel.EmailVerifiedState,
    coolDownTime: Long,
    loadCoolDown: () -> Unit,
    authEvents: SharedFlow<EmailPasswordSignInViewModel.AuthEvent>,
    onVerifyClick: (String) -> Unit,
    navigateToSignUpScreen: (screen: String) -> Unit,
    navigateToProvidePhoneNumberScreen: () -> Unit,
    profileScreenViewModel: ProfileScreenViewModel,
    onLogoutClick: () -> Unit,
    navigateToSettingsCue2FA: () -> Unit,
    openMap: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToOrdersScreen: () -> Unit,
    checkIfEmailIsVerified: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        authEvents.collect { value ->
            when (value) {
                is EmailPasswordSignInViewModel.AuthEvent.Error -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    // za kazdym razem jak wchodzimy na strone ladujemy cooldown, jesli taki jest
    LaunchedEffect(Unit) {
        loadCoolDown()
    }

    var tryLogOut by remember { mutableStateOf(false) }

    val userData by profileScreenViewModel.userData.collectAsState()
    val state by profileScreenViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        println(state)
    }

    LaunchedEffect(Unit) {
        profileScreenViewModel.refreshUser()
        checkIfEmailIsVerified()
    }


    when (userData) {
        null -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            println("user data is null")
        }

        else -> {
            val u = userData!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar + podstawy + skróty
                ProfileHeader(
                    name = u.nameSurname,
                    email = u.email,
                    phone = u.phoneNumber,
                    //onEditProfile = {},
                )

                // Dane kontaktowe
                ProfileInfoCard(
                    coolDownTime = coolDownTime,
                    title = "Contact details",
                    onVerifyClick = { email -> onVerifyClick(email) },
                    rows = profileScreenViewModel.returnRows(),
                    missing =
                        profileScreenViewModel.returnMissing(
                            onMissingEmailClick = {
                                // w settings nie jest zaznaczono enableLinking
                                println("missing email")
                                if (!userData!!.settings.enableLinking) {
                                    navigateToSettingsCue2FA()
                                } else { // enableLinking == true
                                    navigateToSignUpScreen("PROFILE")
                                }
                            },
                            onMissingPhoneNumClick = {
                                println("missing phone number")
                                if (!userData!!.settings.enableLinking) {
                                    navigateToSettingsCue2FA()
                                } else { // enableLinking == true
                                    // showAlertDialogProvideEmailOrPhone = true
                                    navigateToProvidePhoneNumberScreen()
                                }
                            }
                        ),
                    emailVerifiedState = emailVerifiedState,
                )

                // Adres + akcje
                ProfileInfoCard(
                    emailVerifiedState = emailVerifiedState,
                    title = "",
                    trailingActions = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { openMap() }) {
                                Icon(
                                    Icons.Filled.Map,
                                    contentDescription = null,
                                    tint = sagePerSecond
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Show on the map")
                            }
                        }
                    },
                    address = InfoRowData(
                        Icons.Outlined.Home, "Address", combineAddress(
                            city = u.city,
                            woj = convertWojNumberToWojString(u.woj),
                            powiat = u.powiat,
                            street = u.street ?: "",
                            houseNumber = u.houseNumber
                        )
                    ),
                )

                ElevatedActionChip(label = "Orders", onClick = {
                    navigateToOrdersScreen()
                })

                Spacer(Modifier.height(100.dp))

                ElevatedActionChip(label = "Settings", onClick = { navigateToSettings() })

                // Wylogowanie
                OutlinedButton(
                    onClick = { tryLogOut = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = sagePerSecond)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log out")
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (tryLogOut) {
        ConfirmLogoutDialog(
            onConfirm = {
                onLogoutClick()
                tryLogOut = false
            },
            onDismiss = { tryLogOut = false }
        )
    }
}

/* ─────────────────────────  KLOCKI WEWNĘTRZNE  ───────────────────────── */

@Composable
private fun ProfileHeader(
    name: String,
    email: String?,
    phone: String?,
    //onEditProfile: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder (inicjał)
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, sagePerSecond.copy(alpha = 0.25f), CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = sagePerSecond
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = email ?: phone ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                ShortcutPill(text = "Edit profile", onClick = onEditProfile)
//            }
        }
    }
}

data class InfoRowData(
    val icon: ImageVector,
    val label: String,
    val value: String
)

sealed class MissingField(val ctaLabel: String, val onProvide: () -> Unit) {
    class Email(onProvide: () -> Unit) : MissingField("Provide email", onProvide)
    class Phone(onProvide: () -> Unit) : MissingField("Provide phone number", onProvide)
}

@Composable
private fun ProfileInfoCard(
    coolDownTime: Long = 0,
    emailVerifiedState: EmailVerificationViewModel.EmailVerifiedState? = null,
    title: String,
    rows: List<InfoRowData> = emptyList(), // email and phoneNum
    address: InfoRowData? = null,  // sam adress
    missing: MissingField? = null,
    onVerifyClick: (String) -> Unit = {},
    trailingActions: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                trailingActions?.invoke()
            }

            Spacer(Modifier.height(4.dp))

            if (rows.isNotEmpty()) {
                rows.forEach { row ->
                    if (row.label == "E-mail") {
                        InfoRow(row.icon, row.label, row.value, emailVerifiedState = emailVerifiedState)
                        println(coolDownTime)
                        if (emailVerifiedState == EmailVerificationViewModel.EmailVerifiedState.NotVerified) {
                            OutlinedButton(
                                enabled = coolDownTime < 1,
                                onClick = { onVerifyClick(row.value) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = sagePerSecond)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (coolDownTime < 1) "Send email verification" else "Try Again in $coolDownTime")
                            }
                        }
                    } else {
                        InfoRow(row.icon, row.label, row.value)
                    }
                }
            } else if (address != null) {
                InfoRow(address.icon, address.label, address.value)
            }

            if (missing != null) {
                println("missing != null")
                Row {
                    val (icon, label) = when (missing) {
                        is MissingField.Email -> Icons.Outlined.Email to "E-mail"
                        is MissingField.Phone -> Icons.Outlined.Phone to "Phone number"
                    }
                    InfoRow(icon, label, "None")
                    Spacer(Modifier.weight(1f))
                    ShortcutPill(missing.ctaLabel, onClick = { missing.onProvide() })
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    emailVerifiedState: EmailVerificationViewModel.EmailVerifiedState? = null,
) {
    Row(
        Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emailVerifiedState == EmailVerificationViewModel.EmailVerifiedState.Unknown) {
            CircularProgressIndicator()
        } else {
            Icon(
                icon,
                contentDescription = null,
                tint = sagePerSecond,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(16.dp))

                    if (emailVerifiedState != null) {

                        Spacer(Modifier.weight(1f))
                        Text(
                            when (emailVerifiedState) {
                                EmailVerificationViewModel.EmailVerifiedState.Verified -> "E-mail is verified"
                                EmailVerificationViewModel.EmailVerifiedState.NotVerified -> "E-mail is not verified"
                                is EmailVerificationViewModel.EmailVerifiedState.Error -> "Error"
                                EmailVerificationViewModel.EmailVerifiedState.Unknown -> {
                                    "loading..."
                                }
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            when (emailVerifiedState) {
                                is EmailVerificationViewModel.EmailVerifiedState.Error -> Icons.Outlined.Error
                                EmailVerificationViewModel.EmailVerifiedState.NotVerified -> Icons.Outlined.Error
                                EmailVerificationViewModel.EmailVerifiedState.Unknown -> Icons.Outlined.Error
                                EmailVerificationViewModel.EmailVerifiedState.Verified -> Icons.Outlined.Verified
                            },
                            contentDescription = null,
                            tint = if (emailVerifiedState == EmailVerificationViewModel.EmailVerifiedState.Verified) Color.Green else Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ShortcutPill(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            contentColor = sagePerSecond
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ElevatedActionChip(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = sagePerSecond, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ConfirmLogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign out?") },
        text = { Text("Are you sure you want to sign out?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sign out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
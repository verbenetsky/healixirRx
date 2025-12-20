package com.example.pharmacystore.ui.profileScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.combineAddress
import com.example.pharmacystore.common.convertWojNumberToWojString
import com.example.pharmacystore.common.validateEmail
import com.example.pharmacystore.ui.theme.sagePerSecond

@Composable
fun ProfileScreen(
    profileScreenViewModel: ProfileScreenViewModel,
    onLogoutClick: () -> Unit,
    navigateToSettingsCue2FA: () -> Unit,
    openMap: () -> Unit,
    navigateToSettings: () -> Unit,
) {

    var tryLogOut by remember { mutableStateOf(false) }

    val userData by profileScreenViewModel.userData.collectAsState()
    val state by profileScreenViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        println(state)
    }

    var showAlertDialogProvideEmailOrPhone by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf(false) }

    when (userData) {
        null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
                    onEditProfile = {},
                )

                // Dane kontaktowe
                ProfileInfoCard(
                    title = "Contact details",
                    rows = profileScreenViewModel.returnRows(),
                    missing = profileScreenViewModel.returnMissing(
                        onMissingEmailClick = {
                            // w settings nie jest zaznaczono two factor auth
                            if (!userData!!.settings.twoFactorEnabled) {
                                navigateToSettingsCue2FA()
                            } else { // two factor auth == true
                                showAlertDialogProvideEmailOrPhone = true
                                email = true
                            }
                        },
                        onMissingPhoneNumClick = {
                            if (!userData!!.settings.twoFactorEnabled) {
                                navigateToSettingsCue2FA()
                            } else { // two factor auth == true
                                showAlertDialogProvideEmailOrPhone = true
                                phone = true
                            }
                        }
                    )
                )

                // Adres + akcje
                ProfileInfoCard(
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

                ElevatedActionChip(label = "Orders", onClick = { })

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

            if (showAlertDialogProvideEmailOrPhone) {
                ProvideEmailOrPhoneNumber(
                    onDismissRequest = {
                        showAlertDialogProvideEmailOrPhone = false
                    },
                    email = email,
                    phone = phone,
                    onConfirm = { }
                )
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
    onEditProfile: () -> Unit,
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

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShortcutPill(text = "Edit profile", onClick = onEditProfile)
            }
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
    title: String,
    rows: List<InfoRowData> = emptyList(), // email and phoneNum
    address: InfoRowData? = null,  // sam adress
    missing: MissingField? = null,
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
                rows.forEach { InfoRow(it.icon, it.label, it.value) }
            } else if (address != null) {
                InfoRow(address.icon, address.label, address.value)
            }

            if (missing != null) {
                Row {
                    val (icon, label) = when (missing) {
                        is MissingField.Email -> Icons.Outlined.Email to "E-mail"
                        is MissingField.Phone -> Icons.Outlined.Phone to "Phone number"
                    }
                    InfoRow(icon, label, "None")
                    Spacer(Modifier.weight(1f))
                    ShortcutPill(missing.ctaLabel, onClick = missing.onProvide)
                }
            }

        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        Modifier
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = sagePerSecond, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvideEmailOrPhoneNumber(
    onDismissRequest: () -> Unit,
    email: Boolean,
    phone: Boolean,
    onConfirm: (String) -> Unit = {}
) {
    var value by remember { mutableStateOf("") }
    var isOpenPhonePrefixPicker by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier
                .padding(12.dp)
                .widthIn(min = 280.dp, max = 360.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Text(
                    text = "Contact details",
                    style = MaterialTheme.typography.titleLarge
                )
                if (email) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Email") }
                    )
                } else { // todo
//                    OutlinedTextField(
//                        value = "phoneNumber",
//                        onValueChange = { },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier.fillMaxWidth(),
//                        leadingIcon = {
//                            Text(
//                                "",
//                                color = MaterialTheme.colorScheme.onPrimary,
//                                modifier = Modifier
//                                    .padding(4.dp)
//                                    .clickable { isOpenPhonePrefixPicker = true }
//                            )
//                        },
//                        label = { Text("Enter your phone #", color = MaterialTheme.colorScheme.onPrimary) }
//                    )
//
//                    if (isOpenPhonePrefixPicker) {
//                        LazyColumn(Modifier.heightIn(max = 250.dp)) {
//                            items(PhonePrefixesData.listOfPrefixes) { countryRec ->
//                                TextButton(onClick = {
//                                    isOpenPhonePrefixPicker = false
//                                }) {
//                                    Text("${isoToEmoji(countryRec.isoAlpha2)} ${countryRec.name}  +${countryRec.prefix}")
//                                }
//                            }
//                        }
//                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(value)
                            onDismissRequest()
                        },
                        enabled = validateEmail(value)
                    ) {
                        Text("OK")
                    }
                }
            }
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



package com.example.pharmacystore.ui.auth

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.data.local.phoneprefixes.CountryPrefix
import com.example.pharmacystore.data.local.phoneprefixes.PhonePrefixesData
import com.example.pharmacystore.data.local.phoneprefixes.PhonePrefixesData.isoToEmoji
import com.example.pharmacystore.ui.profileSetUp.profileSetupInk
import com.example.pharmacystore.ui.theme.sagePerSecond

@SuppressLint("ContextCastToActivity")
@Composable
fun SmsAuthScreen(
    navigateToSingUpMethodScreen: () -> Unit,
    navigateToHomeScreen: () -> Unit,
    navigateToAuthGate: () -> Unit,
    navigateToProfileSetUp: (String) -> Unit,
    authSmsViewModel: AuthSmsViewModel,
    originScreen: OriginScreen,
) {
    val activity = LocalContext.current as? Activity
        ?: throw IllegalStateException("Composable nie jest osadzone w Activity")

    val phoneNumber by authSmsViewModel.phoneNumber.collectAsState()
    val countryPrefix by authSmsViewModel.countryPrefix.collectAsState()
    val cooldown by authSmsViewModel.remainingSec.collectAsState()
    val uiState by authSmsViewModel.uiState.collectAsState()

    var dialogText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { authSmsViewModel.updateAuthSmsUiState(AuthSmsViewModel.AuthSmsUiState.Idle) }
    }

    LaunchedEffect(Unit) {
        authSmsViewModel.events.collect { value ->
            when (value) {
                is AuthSmsViewModel.AuthSmsUiEvent.EmailSuccessfullyLinked -> {
                    dialogText = value.msg
                    showDialog = true
                }

                is AuthSmsViewModel.AuthSmsUiEvent.PhoneNumberSuccessfullyLinked -> {
                    dialogText = value.msg
                    showDialog = true
                }
            }
        }
    }

    // Side effects na zmiany stanu UI
    LaunchedEffect(uiState) {

        when (val s = uiState) {
            is AuthSmsViewModel.AuthSmsUiState.Failed -> {
                val msg = when (s.message) {
                    AuthSmsViewModel.Err.BAD_PHONE -> "Invalid phone number."
                    AuthSmsViewModel.Err.TOO_MANY -> "Too many attempts. Try again later."
                    AuthSmsViewModel.Err.NO_NETWORK -> "No internet connection."
                    AuthSmsViewModel.Err.GENERIC -> "Unexpected error."
                    AuthSmsViewModel.Err.BAD_CODE -> "The verification code from is invalid."
                }
                snackbarHostState.showSnackbar(msg)
            }

            is AuthSmsViewModel.AuthSmsUiState.Success -> {
                val info = snackFor(originScreen, s.isNew)
                snackbarHostState.showSnackbar(info)
                if (s.isNew) {
                    navigateToProfileSetUp("+${countryPrefix.prefix} $phoneNumber")
                } else {
                    navigateToAuthGate()
                }
            }

            else -> Unit
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
                SmsHeaderCard(
                    title = "SMS authentication",
                    subtitle = "Confirm your phone number to continue."
                )

                when (val s = uiState) {

                    is AuthSmsViewModel.AuthSmsUiState.Idle,

                    is AuthSmsViewModel.AuthSmsUiState.Failed -> {
                        EnterPhoneNumber(
                            phoneNumber = phoneNumber,
                            countryPrefix = countryPrefix,
                            updatePhoneNumber = { authSmsViewModel.updatePhoneNumber(it) },
                            updateCountryPrefix = { authSmsViewModel.updateCountryPrefixData(it) },
                            remainingSec = cooldown,
                            onLoadCooldown = { authSmsViewModel.loadCooldown(it) },
                            sendConfirmationSms = { fullPhone ->
                                authSmsViewModel.sendSms(fullPhone, activity)
                                // cooldown ustawi VM po SuccessSend (onCodeSent)
                            },
                            error = if (s is AuthSmsViewModel.AuthSmsUiState.Failed) when (s.message) {
                                AuthSmsViewModel.Err.BAD_PHONE -> "Invalid phone number."
                                AuthSmsViewModel.Err.TOO_MANY -> "Too many attempts. Try again later."
                                AuthSmsViewModel.Err.NO_NETWORK -> "No internet connection."
                                AuthSmsViewModel.Err.GENERIC -> "Unexpected error."
                                AuthSmsViewModel.Err.BAD_CODE -> "The verification code from is invalid."
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is AuthSmsViewModel.AuthSmsUiState.FailedLinking -> {
                        EnterPhoneNumber(
                            phoneNumber = phoneNumber,
                            countryPrefix = countryPrefix,
                            updatePhoneNumber = { authSmsViewModel.updatePhoneNumber(it) },
                            updateCountryPrefix = { authSmsViewModel.updateCountryPrefixData(it) },
                            remainingSec = cooldown,
                            onLoadCooldown = { authSmsViewModel.loadCooldown(it) },
                            sendConfirmationSms = { fullPhone ->
                                authSmsViewModel.sendSms(fullPhone, activity)
                                // cooldown ustawi VM po SuccessSend (onCodeSent)
                            },
                            error = when (val e = s.message) {
                                LinkPhoneError.CodeExpired ->
                                    "The verification code has expired. Please request a new SMS code and try again."

                                LinkPhoneError.InvalidCode ->
                                    "Incorrect verification code. Check the SMS and try again."

                                LinkPhoneError.Network ->
                                    "No internet connection. Please check your network and try again."

                                LinkPhoneError.NotLoggedIn ->
                                    "Your session has expired. Please sign in again and then link your phone number."

                                LinkPhoneError.PhoneAlreadyInUse ->
                                    "This phone number is already linked to another account. Please use a different number or sign in with this phone number."

                                LinkPhoneError.TooManyRequests ->
                                    "Too many attempts. Please wait a few minutes and try again."

                                is LinkPhoneError.Unknown ->
                                    e.message?.takeIf { it.isNotBlank() }
                                        ?: "Something went wrong while linking your phone number. Please try again."
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is AuthSmsViewModel.AuthSmsUiState.Loading -> {
                        SmsSectionCard(
                            title = "Please wait",
                            subtitle = "We are processing your request."
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.DarkGray)
                            }
                        }
                    }

                    is AuthSmsViewModel.AuthSmsUiState.SuccessSend -> {
                        val verificationId = s.verificationId
                        EnterConfirmationCode(
                            onCheckClick = { code ->
                                println("code is $code")
                                if (originScreen == OriginScreen.PROFILE) {
                                    authSmsViewModel.linkPhoneNumToEmail(code, verificationId)
                                } else {
                                    authSmsViewModel.verifySms(verificationId, code)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is AuthSmsViewModel.AuthSmsUiState.Success -> {
                        SmsSectionCard(
                            title = "Finishing",
                            subtitle = "Finalizing authentication."
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                Spacer(Modifier.height(90.dp))
            }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        )
    }

    DoubleBackReact(
        exit = {
            when (originScreen) {
                OriginScreen.PROFILE -> {
                    navigateToHomeScreen()
                }
                else -> {
                    when (uiState) {
                        is AuthSmsViewModel.AuthSmsUiState.Idle,
                        is AuthSmsViewModel.AuthSmsUiState.Failed -> navigateToSingUpMethodScreen()

                        else -> authSmsViewModel.updateAuthSmsUiState(AuthSmsViewModel.AuthSmsUiState.Idle)
                    }
                }
            }
        },
        message =
            when (originScreen) {
                OriginScreen.PROFILE -> {
                    "Press back again to return to home screen"
                }

                else -> {
                    if (uiState is AuthSmsViewModel.AuthSmsUiState.Idle)
                        "Press back again to return to starting screen"
                    else
                        "Press back again to return to phone-number entering screen"
                }
            }

    )

    if (showDialog) {
        InfoDialog(
            text = dialogText,
            onDismiss = {
                showDialog = false
                navigateToHomeScreen()
            }
        )
    }
}

@Composable
fun EnterPhoneNumber(
    modifier: Modifier = Modifier,
    phoneNumber: String,
    countryPrefix: CountryPrefix,
    updatePhoneNumber: (String) -> Unit,
    updateCountryPrefix: (CountryPrefix) -> Unit,
    remainingSec: Long,                          // 0 = brak cooldownu
    onLoadCooldown: (String) -> Unit,            // VM.loadCooldown(fullPhone)
    sendConfirmationSms: (String) -> Unit,       // VM.sendSms(fullPhone, activity)
    error: String? = null,
) {
    val ink = smsAuthInk()
    val tfColors = smsAuthOutlinedTextFieldColors()

    var isOpenPhonePrefixPicker by remember { mutableStateOf(false) }
    val fullPhone = remember(countryPrefix, phoneNumber) { "+${countryPrefix.prefix}$phoneNumber" }

    // Za każdym razem, gdy zmienia się pełny numer, ładujemy cooldown dla TEGO numeru
    LaunchedEffect(fullPhone) {
        if (phoneNumber.isNotBlank()) onLoadCooldown(fullPhone)
    }

    SmsSectionCard(
        title = "Phone number",
        subtitle = "Select your prefix and enter the number.",
        modifier = modifier
    ) {
        Text(
            text = "Country prefix",
            color = ink.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(1.dp))

        Surface(
            onClick = { isOpenPhonePrefixPicker = !isOpenPhonePrefixPicker },
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.18f),
            border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${isoToEmoji(countryPrefix.isoAlpha2)}  +${countryPrefix.prefix}",
                    color = ink,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (isOpenPhonePrefixPicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = sagePerSecond
                )
            }
        }

        if (isOpenPhonePrefixPicker) {
            SmsSuggestionsSurface {
                LazyColumn(Modifier.heightIn(max = 260.dp)) {
                    items(PhonePrefixesData.listOfPrefixes) { countryRec ->
                        SmsSuggestionRow(
                            title = "${isoToEmoji(countryRec.isoAlpha2)} ${countryRec.name}",
                            subtitle = "+${countryRec.prefix}",
                            onClick = {
                                updateCountryPrefix(countryRec)
                                isOpenPhonePrefixPicker = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Phone number",
            color = ink.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(1.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = updatePhoneNumber,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Filled.Phone,
                    contentDescription = null,
                    tint = sagePerSecond
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = tfColors,
            placeholder = { Text("e.g. 501234567", color = profileSetupInk().copy(alpha = 0.7f)) }
        )

        Spacer(Modifier.height(10.dp))

        if (remainingSec > 0) {
            Text("Try again in ${remainingSec}s", color = ink.copy(alpha = 0.80f))
            Spacer(Modifier.height(6.dp))
        }

        Button(
            onClick = { sendConfirmationSms(fullPhone) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 1.dp,
                hoveredElevation = 8.dp,
                focusedElevation = 8.dp,
                disabledElevation = 0.dp
            ),
            enabled = phoneNumber.isNotBlank() && remainingSec == 0L
        ) {
            Text("Send confirmation sms")
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(error, color = ink, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun EnterConfirmationCode(
    onCheckClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tfColors = smsAuthOutlinedTextFieldColors()

    var confirmationCode by remember { mutableStateOf("") }

    SmsSectionCard(
        title = "Confirmation code",
        subtitle = "Enter the code you received via SMS.",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = confirmationCode,
            onValueChange = { confirmationCode = it },
            label = { Text("Enter confirmation code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(
                    Icons.Filled.Key,
                    contentDescription = null,
                    tint = sagePerSecond
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = tfColors
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { onCheckClick(confirmationCode) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 1.dp,
                hoveredElevation = 8.dp,
                focusedElevation = 8.dp,
                disabledElevation = 0.dp
            ),
            enabled = confirmationCode.isNotBlank()
        ) { Text("Enter code") }
    }
}

/* ─────────────────────────  UI  ───────────────────────── */

@Composable
private fun smsAuthInk(): Color = Color(0xFF1B1B12)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun smsAuthOutlinedTextFieldColors(): TextFieldColors {
    val ink = smsAuthInk()
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
private fun SmsHeaderCard(
    title: String,
    subtitle: String
) {
    val ink = smsAuthInk()

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
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(1.dp, sagePerSecond.copy(alpha = 0.25f), CircleShape)
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = sagePerSecond
                        )
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
}

@Composable
private fun SmsSectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val ink = smsAuthInk()

    Card(
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun SmsSuggestionsSurface(
    content: @Composable () -> Unit
) {
    val ink = smsAuthInk()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(returnGradientBackGround())
        ) {
            Column(Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun SmsSuggestionRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    val ink = smsAuthInk()

    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.18f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 15.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = ink
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ink.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDialog(
    text: String,
    onDismiss: () -> Unit,
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
                    text = "Information",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { onDismiss() }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}


enum class OriginScreen { SIGN_UP, SIGN_IN, PROFILE }

private fun snackFor(origin: OriginScreen, isNew: Boolean): String =
    when (origin) {
        OriginScreen.SIGN_UP ->
            if (isNew) "This is a new phone number — complete your profile to finish sign-up."
            else "Signed in — this phone number is already linked to an account."

        OriginScreen.SIGN_IN ->
            if (isNew) "This is a new phone number — complete your profile to finish sign-up."
            else "Signed in — this phone number is already linked to an account."

        else -> ""
    }

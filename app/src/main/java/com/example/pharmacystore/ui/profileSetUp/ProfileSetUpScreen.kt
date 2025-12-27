package com.example.pharmacystore.ui.profileSetUp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.common.convertWojNumberToWojString
import com.example.pharmacystore.common.millisToDateString
import com.example.pharmacystore.common.returnGradientBackGround
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import com.google.firebase.auth.ktx.auth
import com.example.pharmacystore.ui.theme.sagePerSecond
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.pharmacystore.ui.theme.sagePerFirst
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileSetUpScreen(
    profileSetUpViewModel: ProfileSetUpViewModel,
    navigateToHomeScreen: () -> Unit,
    navigateToSignIn: () -> Unit,
    phoneNumber: String?,
    email: String?
) {
    val ink = profileSetupInk()
    val ctx = LocalContext.current
    val activity = ctx as Activity
    val uid = Firebase.auth.currentUser?.uid

    // State
    var cityDataSym by rememberSaveable { mutableStateOf("") }
    var expandedCities by remember { mutableStateOf(false) }
    var expandedStreet by remember { mutableStateOf(false) }

    val cities by profileSetUpViewModel.citiesVillagesDetails.collectAsStateWithLifecycle(emptyList())
    val streets by profileSetUpViewModel.streetsDetails.collectAsStateWithLifecycle(emptyList())
    val userInformation by profileSetUpViewModel.userInformation.collectAsStateWithLifecycle()
    val hasStreetsState by profileSetUpViewModel.hasStreets.collectAsStateWithLifecycle()

    val uiState by profileSetUpViewModel.uiState.collectAsState(initial = ProfileSetUpViewModel.UiEventAuth.Idle)

    val tfColors = profileSetupOutlinedTextFieldColors()

    // jesli sie okaze ze uid jest null to zeby apka sie nie wywalila a user zostal przekierowany do ekranu logowania
    if (uid == null) {
        LaunchedEffect(Unit) { navigateToSignIn() }
        return // nie renderuj reszty ekranu
    }

    LaunchedEffect(userInformation) {
        println(userInformation)
    }

    // automatycznie tworzy dokument w firestore przy utworzeniu konta
    LaunchedEffect(Unit) {
        profileSetUpViewModel.onEnterScreen(phoneNumber, email)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileSetUpViewModel.UiEventAuth.Error -> {
                val error = (uiState as ProfileSetUpViewModel.UiEventAuth.Error).msg
                Toast.makeText(ctx, error, Toast.LENGTH_LONG).show()
            }

            ProfileSetUpViewModel.UiEventAuth.Success -> {
                Toast.makeText(
                    ctx,
                    "You've successfully configured your profile",
                    Toast.LENGTH_LONG
                ).show()
                navigateToHomeScreen()
            }

            else -> Unit
        }
    }

    DoubleBackReact(exit = { activity.finish() })

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

            SetupHeaderCard(
                title = "Profile setup",
                subtitle = email ?: phoneNumber ?: "Complete the required fields to continue."
            )

            SetupSectionCard(
                title = "Basic information",
                subtitle = "This will be visible on your profile."
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        text = "Enter first name and last name",
                        color = ink.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(1.dp))

                    OutlinedTextField(
                        value = userInformation.nameSurname,
                        onValueChange = { profileSetUpViewModel.updateUser { copy(nameSurname = it) } },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = sagePerSecond
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    var showDatePicker by remember { mutableStateOf(false) }

                    Text(
                        text = "Enter birth date",
                        color = ink.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(1.dp))

                    OutlinedTextField(
                        value = if (userInformation.birthday != null) millisToDateString(
                            userInformation.birthday!!
                        ) else "",
                        onValueChange = {},
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = sagePerFirst.copy(alpha = 0.9f)
                                )
                            }
                        },
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showDatePicker) {
                        DatePickerModalInput(onDateSelected = { date ->
                            if (date != null) {
                                profileSetUpViewModel.updateUser { copy(birthday = date) }
                            } else {
                                profileSetUpViewModel.updateUser { copy(birthday = -100) }
                            }
                        }) {
                            showDatePicker = false
                        }
                    }
                }
            }

            SetupSectionCard(
                title = "Address",
                subtitle = "We use this to show pharmacies and deliveries near you."
            ) {

                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        text = "Choose the city or village",
                        color = ink.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(1.dp))

                    OutlinedTextField(
                        value = userInformation.city,
                        onValueChange = {
                            profileSetUpViewModel.updateUser { copy(city = it) }
                            expandedCities = true
                            profileSetUpViewModel.findSimilarCitiesOrVillages(it)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = sagePerSecond
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (expandedCities && cities.isNotEmpty() && userInformation.city.isNotEmpty()) {
                        SuggestionsSurface {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                items(cities) { item ->
                                    SuggestionRow(
                                        title = item.name,
                                        subtitle = "${convertWojNumberToWojString(item.woj)}, pow. ${item.powName}",
                                        onClick = {
                                            profileSetUpViewModel.updateUser {
                                                copy(
                                                    email = email,
                                                    phoneNumber = phoneNumber,
                                                    city = item.name,
                                                    woj = item.woj,
                                                    powiat = item.powName,
                                                    userId = uid,
                                                    configurationCompleted = true,
                                                )
                                            }

                                            profileSetUpViewModel.hasStreets(item.sym) // sprawdzamy czy jest
                                            cityDataSym = item.sym
                                            expandedCities = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (hasStreetsState) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        Text(
                            text = "Choose the street",
                            color = ink.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(1.dp))

                        OutlinedTextField(
                            value = userInformation.street ?: "",
                            onValueChange = {
                                profileSetUpViewModel.updateUser { copy(street = it) }
                                expandedStreet = true
                                profileSetUpViewModel.findSimilarStreet(it, cityDataSym)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Home,
                                    contentDescription = null,
                                    tint = sagePerSecond
                                )
                            },
                            singleLine = true,
                            enabled = cityDataSym.isNotEmpty() && userInformation.city.isNotEmpty(),
                            shape = RoundedCornerShape(16.dp),
                            colors = tfColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (expandedStreet && streets.isNotEmpty() && userInformation.street?.isNotEmpty() != false) {
                            SuggestionsSurface {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 230.dp)
                                ) {
                                    items(streets) { item ->
                                        SuggestionRow(
                                            title = item.name,
                                            subtitle = null,
                                            onClick = {
                                                profileSetUpViewModel.updateUser { copy(street = item.name) }
                                                expandedStreet = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // jest to tutaj potrezbne gdyz jesli user najpierw wybierze miasto co ma ulicy np. Wawa
                    // potem wpisze jakas ulice, potem wybierze jakas wies to ta ulica zostanie zapisana
                    profileSetUpViewModel.updateUser { copy(street = null) }
                }

                Spacer(Modifier.height(10.dp))

                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = "House / apartment number",
                        color = ink.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(Modifier.height(1.dp))

                    OutlinedTextField(
                        value = userInformation.houseNumber,
                        onValueChange = { profileSetUpViewModel.updateUser { copy(houseNumber = it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }

            Button(
                onClick = {
                    profileSetUpViewModel.saveUser(userInformation)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 1.dp,
                    hoveredElevation = 8.dp,
                    focusedElevation = 8.dp,
                    disabledElevation = 0.dp
                ),
                enabled = userInformation.city.isNotEmpty() &&
                        userInformation.houseNumber.isNotEmpty() &&
                        userInformation.nameSurname.isNotEmpty() && userInformation.birthday != null
            ) {
                Text("Continue")
            }

            Spacer(Modifier.height(90.dp))
        }
    }
}

/* ─────────────────────────  UI  ───────────────────────── */

@Composable
fun profileSetupInk(): Color {
    return Color(0xFF1B1B12)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun profileSetupOutlinedTextFieldColors(): TextFieldColors {
    val ink = profileSetupInk()
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
private fun SetupHeaderCard(
    title: String,
    subtitle: String
) {
    val ink = profileSetupInk()

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
                        Icon(Icons.Filled.Person, contentDescription = null, tint = sagePerSecond)
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
private fun SetupSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val ink = profileSetupInk()

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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = {
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
            )
        }
    }
}

@Composable
private fun SuggestionsSurface(
    content: @Composable () -> Unit
) {
    val ink = profileSetupInk()

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
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    val ink = profileSetupInk()

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

/* ─────────────────────────  DATE PICKER  ───────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}



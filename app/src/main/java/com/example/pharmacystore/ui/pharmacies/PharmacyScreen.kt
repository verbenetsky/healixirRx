package com.example.pharmacystore.ui.pharmacies

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.pharmacystore.common.calculateDistance
import com.example.pharmacystore.common.combineAddress
import com.example.pharmacystore.data.remote.FullPharmacyDto
import com.example.pharmacystore.data.remote.PharmacyShort
import com.example.pharmacystore.ui.drug.DrugViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.revenuecat.placeholder.PlaceholderDefaults
import com.revenuecat.placeholder.placeholder
import kotlinx.coroutines.delay

@Composable
fun PharmacyScreen(
    latLong: LatLng?,
    pharmacyViewModel: PharmacyViewModel,
    drugViewModel: DrugViewModel,
    onBack: () -> Unit = {},
    navigateToPharmacyStock: () -> Unit,
) {
    val ctx = LocalContext.current
    val state by pharmacyViewModel.pharmacyDetails.collectAsState()

    Scaffold(
        topBar = {
            TopBarForState(
                state = state,
                onBack = onBack
            )
        },
    ) { padding ->
        Crossfade(targetState = state, label = "pharmacy_state") { s ->
            when (s) {
                PharmacyViewModel.DetailsState.Idle,
                PharmacyViewModel.DetailsState.Loading -> {
                    Body(
                        padding = padding,
                        data = FullPharmacyDto.EMPTY,
                        ctx = ctx,
                        isLoading = true,
                        navigateToPharmacyStock = navigateToPharmacyStock
                    )
                }

                is PharmacyViewModel.DetailsState.Success -> {
                    Body(
                        padding = padding,
                        data = s.pharmacy,
                        ctx = ctx,
                        isLoading = false,
                        navigateToPharmacyStock = {
                            navigateToPharmacyStock()
                            drugViewModel.getPharmacyStock(s.pharmacy.identyfikator_apteki)
                            pharmacyViewModel.savePharmacyInfo(
                                info = PharmacyShort(
                                    id = s.pharmacy.identyfikator_apteki,
                                    name = s.pharmacy.nazwa_apteki ?: "Apteka",
                                    address = s.pharmacy.ulica_znormalizowana,
                                    city = s.pharmacy.miejscowosc ?: "",
                                    distanceKms = calculateDistance(
                                        lat1 = s.pharmacy.lat,
                                        lon1 = s.pharmacy.lon,
                                        lat2 = if (latLong?.latitude != null) latLong.latitude else 0.0,
                                        lon2 = if (latLong?.longitude != null) latLong.longitude else 0.0
                                    )
                                )
                            )
                            println("lon is: ${s.pharmacy.lon}")
                            println("lat is: ${s.pharmacy.lat}")
                        }
                    )
                }

                is PharmacyViewModel.DetailsState.Error -> {
                    Body(
                        padding = padding,
                        data = FullPharmacyDto.EMPTY,
                        ctx = ctx,
                        isLoading = true,
                        navigateToPharmacyStock = { }
                    )

                    LaunchedEffect(s) {
                        while (true) {
                            Toast.makeText(
                                ctx,
                                s.msg,
                                Toast.LENGTH_LONG
                            ).show()
                            delay(4_000)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarForState(
    state: PharmacyViewModel.DetailsState,
    onBack: () -> Unit
) {
    val titleText = when (state) {
        is PharmacyViewModel.DetailsState.Success -> {
            if (!state.pharmacy.nazwa_apteki.isNullOrBlank()) state.pharmacy.nazwa_apteki else "Apteka"
        }

        else -> ""
    }

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),

        title = {
            Text(
                titleText,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}


@Composable
private fun Body(
    padding: PaddingValues,
    data: FullPharmacyDto,
    ctx: Context,
    isLoading: Boolean = false,
    navigateToPharmacyStock: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        /* -------- Sekcja Informacje -------- */
        item {
            SectionCard(
                title = "Informacje",
                icon = Icons.Default.Info,
                isLoading = isLoading
            ) {
                TwoLineRow("Stan", data.stan_apteki.orDash())
                TwoLineRow("Rodzaj", data.rodzaj_apteki.orDash())
                TwoLineRow("Data uruchomienia", data.data_uruchomienia_apteki.orDash())

                Spacer(Modifier.height(12.dp))
                TwoLineRow("Właściciel", data.wlasciciel_nazwa.orDash())
                Spacer(Modifier.height(12.dp))
            }
        }

        /* -------- Sekcja Dostępność leków -------- */
        item {
            SectionCard(
                title = "Medicine availability",
                icon = Icons.Default.Medication,
                isLoading = isLoading
            ) {
                FilledTonalButton(
                    onClick = navigateToPharmacyStock,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text("View stock")
                }
            }
        }

        /* -------- Sekcja Kontakt (telefon / email) -------- */
        if (!data.telefon.isNullOrBlank() || !data.email.isNullOrBlank()) {
            item {
                SectionCard(
                    title = "Kontakt",
                    icon = Icons.Default.Info,
                    isLoading = isLoading
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .placeholder(isLoading)
                    ) {
                        if (!data.telefon.isNullOrBlank()) {
                            TwoLineRow("Telefon", data.telefon.orDash())
                            Spacer(Modifier.height(15.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val u = "tel:${data.telefon}".toUri()
                                        val i = Intent(Intent.ACTION_DIAL, u)
                                        ctx.startActivity(i)
                                    } catch (_: SecurityException) {
                                        Toast.makeText(
                                            ctx,
                                            "Nie można uruchomić telefonu",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } catch (_: Exception) {
                                        Toast.makeText(ctx, "Wystąpił błąd", Toast.LENGTH_LONG)
                                            .show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.LocalPhone,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Call")
                            }
                        }

                        if (!data.email.isNullOrBlank()) {
                            Spacer(Modifier.height(24.dp))
                            TwoLineRow("Email", data.email.orDash().lowercase())
                            Spacer(Modifier.height(15.dp))
                            OutlinedButton(
                                onClick = {
                                    val uri = "mailto:${Uri.encode(data.email)}"
                                    val intent = Intent(Intent.ACTION_SENDTO, uri.toUri())
                                    try {
                                        ctx.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                "Wyślij e-mail…"
                                            )
                                        )
                                    } catch (_: Exception) {
                                        Toast.makeText(
                                            ctx,
                                            "Brak klienta e-mail",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Send Email")
                            }
                        }
                    }
                }
            }
        }

        /* -------- Godziny otwarcia -------- */
        val hours = listOf(
            "Monday" to data.godziny_otwarcia_poniedzialek,
            "Tuesday" to data.godziny_otwarcia_wtorek,
            "Wednesday" to data.godziny_otwarcia_sroda,
            "Thursday" to data.godziny_otwarcia_czwartek,
            "Friday" to data.godziny_otwarcia_piatek,
            "Saturday" to data.godziny_otwarcia_sobota
        )
        val hasAnyHours = hours.any { !it.second.isNullOrBlank() }

        if (hasAnyHours) {
            item {
                SectionCard(
                    title = "Godziny otwarcia",
                    icon = Icons.Default.AccessTime,
                    isLoading = isLoading
                ) {
                    HoursList(hours)
                }
            }
        } else {
            item {
                SectionCard(
                    title = "Opening hours are unavailable.\nPlease contact the pharmacy for accurate information.",
                    icon = Icons.Default.AccessTime,
                    content = {}
                )
            }
        }

        /* -------- Mapa + Nawigacja -------- */
        if (data.lat != null && data.lon != null) {
            item {
                PharmacyMap(
                    address = combineAddress(
                        city = data.miejscowosc ?: "",
                        woj = data.wojewodztwo,
                        powiat = data.powiat,
                        street = data.nazwa_ulicy ?: "",
                        houseNumber = data.numer_budynku ?: ""
                    ),
                    data = data,
                    isLoading = isLoading,
                    latLng = LatLng(data.lat, data.lon)
                )

                Spacer(Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = {
                        mapIntent(
                            lat = data.lat,
                            lon = data.lon,
                            name = "${data.ulica_znormalizowana ?: ""} Apteka".trim(),
                            ctx = ctx
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text("Navigate")
                }
            }
        }

        if (isLoading) {
            item {
                SectionCard(
                    title = "Godziny otwarcia",
                    icon = Icons.Default.AccessTime,
                    isLoading = isLoading
                ) {
                    HoursList(hours)
                }
            }
            item {
                SectionCard(
                    title = "Godziny otwarcia",
                    icon = Icons.Default.AccessTime,
                    isLoading = isLoading
                ) {
                    HoursList(hours)
                }
            }
        }
    }
}


@Composable
fun SectionCard(
    title: String? = null,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .placeholder(
                enabled = isLoading,
                shape = RoundedCornerShape(16.dp),
                highlight = PlaceholderDefaults.pulse
            )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))

                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun TwoLineRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = if (value.contains(".") || value.contains("-")) value else
                value.lowercase().replaceFirstChar { it.titlecase() },

            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp)
        )
    }
}

@Composable
private fun HoursList(items: List<Pair<String, String?>>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (day, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (!value.isNullOrBlank()) {
                    Text(day, fontWeight = FontWeight.Medium)
                    Text(value.orDash())
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun PharmacyMap(
    address: String,
    data: FullPharmacyDto,
    latLng: LatLng,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 16f) // start
    }

    var mapReady by remember { mutableStateOf(false) }
    LaunchedEffect(latLng, mapReady) {
        if (mapReady) {
            try {
                camera.animate(
                    update = CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                    durationMs = 600
                )
            } catch (_: IllegalStateException) {
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .size(800.dp),
    ) {
        // Adres
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .placeholder(
                    isLoading,
                    shape = RoundedCornerShape(16.dp),
                ),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {

            SectionCard(
                title = "Adres",
                icon = Icons.Default.Home,
                isLoading = isLoading
            ) {
                val ulica = buildString {
                    append((data.typ_ulicy ?: "").trim().withSpaceSuffix())
                    append((data.nazwa_ulicy ?: data.ulica_znormalizowana ?: "").trim())
                }.ifBlank { null }

                val numer = listOfNotNull(
                    data.numer_budynku?.takeIf { it.isNotBlank() },
                    data.numer_lokalu?.takeIf { it.isNotBlank() }?.let { "lok. $it" }
                ).joinToString(" ").ifBlank { null }

                val miasto = listOfNotNull(
                    data.miejscowosc?.takeIf { it.isNotBlank() },
                    data.kod_pocztowy?.takeIf { it.isNotBlank() }
                ).joinToString(" • ").ifBlank { null }

                TwoLineRow("Ulica", ulica.orDash())
                TwoLineRow("Nr", numer.orDash())
                TwoLineRow("Miejscowość / kod", miasto.orDash())
                TwoLineRow("Poczta", data.poczta.orDash())
                TwoLineRow("Województwo", data.wojewodztwo.orDash())
                TwoLineRow("Powiat", data.powiat.orDash())
                TwoLineRow("Gmina", data.gmina.orDash())
            }

        }

        Spacer(Modifier.height(15.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                onMapLoaded = { mapReady = true }
            ) {
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Lokalizacja",
                    snippet = address
                )
            }
        }
    }
}

fun String?.orDash(): String = if (this.isNullOrBlank()) "—" else this
private fun String.withSpaceSuffix(): String = if (isBlank()) "" else "$this "

package com.example.pharmacystore.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.SharedFlow


// ten ekran jest wsm taki sam jak w authSmsScreen kiedy sie wpisuje kod ale pewnie lepiej to rozdzielic
// zeby czysciej wygladalo
@Composable
fun TwoFAScreenCode(
    twoFaState: SettingsViewModel.TwoFaState,
    twoFaEvents: SharedFlow<SettingsViewModel.TwoFaEvents>,
    completeEnrollment: (String) -> Unit,
    navigateToSettingsScreen: () -> Unit,
    navigateToProfileScreen: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        twoFaEvents.collect { value ->
            when(value) {
                SettingsViewModel.TwoFaEvents.NavigateToProfileScreen -> {
                    navigateToProfileScreen()
                    Toast.makeText(context, "2FA Successfully enabled", Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    DoubleBackReact(
        message = "Press back again to return to settings screen",
        exit = { navigateToSettingsScreen() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(returnGradientBackGround())
    ) {
        when(twoFaState) {
            SettingsViewModel.TwoFaState.Enrolling -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    EnterConfirmationCode(
                        onCheckClick = { code -> completeEnrollment(code) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
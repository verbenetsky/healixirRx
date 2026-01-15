package com.example.pharmacystore.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.ui.auth.signIn.EmailPasswordSignInViewModel
import kotlinx.coroutines.flow.SharedFlow

// ten ekran posiada identyczne zachowanie jak SmsAuthScreen (czesc gdzie trzeba wpisywac sms code)
// i taki sam jak TwoFAScreenCode, ale ma troche inny use case
// troche jest tutaj naruszone DRY ale zato zyskuje sie czytelnosc

@Composable
fun SignInMfaSmsCode(
    mfaEvents: SharedFlow<EmailPasswordSignInViewModel.MfaEvents>,
    mfaState: EmailPasswordSignInViewModel.MfaState,
    navigateToMain: () -> Unit,
    navigateToSignInScreen: () -> Unit,
    verifyMfaSmsCode: (code: String) -> Unit,
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        mfaEvents.collect { value ->
            when (value) {
                EmailPasswordSignInViewModel.MfaEvents.NavigateToMain -> {
                    navigateToMain()
                    Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DoubleBackReact(
        message = "Press back again to return to Sign in screen",
        exit = { navigateToSignInScreen() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(returnGradientBackGround())
    ) {
        when (mfaState) {

            is EmailPasswordSignInViewModel.MfaState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    EnterConfirmationCode(
                        onCheckClick = { code -> verifyMfaSmsCode(code) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.4.dp, Color(0xFF1B1B12)),
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
                            Text(mfaState.msg, color = Color(0xFF1B1B12), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            EmailPasswordSignInViewModel.MfaState.Idle -> { }

            EmailPasswordSignInViewModel.MfaState.MfaSmsCodeSent -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    EnterConfirmationCode(
                        onCheckClick = { code -> verifyMfaSmsCode(code) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            EmailPasswordSignInViewModel.MfaState.Loading -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
package com.example.pharmacystore.ui.auth.signIn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect


// ekran ktory zawsze pierwszy sie odpala po tym jak user zamknac aplikacje
// jego celem jest sprawdzenie czy user jest zalogowany (czy lokalnie jest przechowywany jakis token firebase auth)
// jesli jest to user zostanie przekierowany na home screen a jesli nie to do logowania

@Composable
fun CheckIfUserLoggedIn(
    isLoggedIn: Boolean?,
    navigateToLogInScreen: () -> Unit,
    navigateToAuthGateScreen: () -> Unit
) {
    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> {
                println("navigate to auth gateScreen")
                navigateToAuthGateScreen()
            }

            false -> {
                navigateToLogInScreen()
            }
            null -> Unit
        }
    }
}
package com.example.pharmacystore.ui.auth.signIn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// ekran posredniczacy pomiedzy logowanie a profileSetUp/HomeScreen
// jesli sie okaze ze user nie dokonczyl profileSetUp
// (czyli stworzyl konto ale po tym jak zostal przekierowany na profileSetup screen poprostu opuscil apke)
// to przekierowujemy go do profileSetUp
// jesli ukonczyl to do HomeScreen

@Composable
fun AuthGate(
    viewModel: AuthGateViewModel,
    navigateToProfileSetup: (Pair<String?, String?>) -> Unit, // first jest czy to email czy to phone number, second sama wartosc
    navigateToHomeScreen: () -> Unit
) {
    val uid = Firebase.auth.currentUser?.uid ?: return
    val states by viewModel.state.collectAsState()

    LaunchedEffect(uid) {
        viewModel.decideWhereToGo(uid)
    }


    LaunchedEffect(Unit) {
        viewModel.events.collect { data ->

            when (data) {
                AuthGateViewModel.AuthGateEvent.NavigateToHomeScreen -> {
                    navigateToHomeScreen()
                }

                is AuthGateViewModel.AuthGateEvent.NavigateToProfileSetUp -> {
                    navigateToProfileSetup(Pair(data.contact.first, data.contact.second))
                }
            }
        }
    }

    DisposableEffect(Unit) {
        viewModel.startListenForUser(uid)
        onDispose { viewModel.onCleared() }
    }

    when (val s = states) {
        is AuthGateViewModel.AuthGateState.Error -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(s.msg)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    viewModel.retry(uid)
                }) { Text("Try again") }
            }
        }

        AuthGateViewModel.AuthGateState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
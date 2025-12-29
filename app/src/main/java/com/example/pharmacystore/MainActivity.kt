package com.example.pharmacystore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pharmacystore.common.AppGradientRoot
import com.example.pharmacystore.common.MainScaffold
import com.example.pharmacystore.common.toNavGraphOrNull
import com.example.pharmacystore.ui.theme.PharmacyStoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.Transparent.toArgb(),
                )
        )
        setContent {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val graphRoute = backStackEntry
                ?.destination
                ?.parent
                ?.route

            val route = backStackEntry
                ?.destination
                ?.route

            AppGradientRoot(graphRoute.toNavGraphOrNull(), route) {
                PharmacyStoreTheme { MainScaffold(navController = navController) }
            }
        }
    }
}
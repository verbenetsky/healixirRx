package com.example.pharmacystore.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pharmacystore.R
import com.example.pharmacystore.ui.auth.AuthSmsViewModel
import com.example.pharmacystore.ui.auth.signIn.AuthGateViewModel
import com.example.pharmacystore.ui.auth.signIn.EmailPasswordSignInViewModel
import com.example.pharmacystore.ui.auth.signUp.EmailPasswordSignUpViewModel
import com.example.pharmacystore.ui.drug.DrugViewModel
import com.example.pharmacystore.ui.shoppingcart.ShoppingCartViewModel
import com.example.pharmacystore.ui.map.MapViewModel
import com.example.pharmacystore.ui.pharmacies.PharmacyViewModel
import com.example.pharmacystore.ui.profileScreen.ProfileScreenViewModel
import com.example.pharmacystore.ui.profileSetUp.ProfileSetUpViewModel
import com.example.pharmacystore.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val parentGraphRoute = backStackEntry
        ?.destination
        ?.parent
        ?.route // ⇒ "auth_graph"

    val route = backStackEntry
        ?.destination
        ?.route

    val showBottomBar =
        route != Screen.Settings.route && route != Screen.Map.route

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(route) {
        scrollBehavior.state.heightOffset =
            0f // bierzace przewiniecie topAppBar, jesli 0f w pelni widoczna, jesli ujemna to czesciowa schowana
        scrollBehavior.state.contentOffset =
            0f // jesli 0f to ekran jest na samej gorze i nie jest przewiniety, jesli offset > 0f to jest przewiniecie do dolu
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            if (parentGraphRoute != "auth_graph") {
                val fraction = scrollBehavior.state.overlappedFraction
                val base = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
                val container = base.copy(alpha = 1f - fraction)

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    TopAppBar(
                        colors = topAppBarColors(
                            containerColor = container,
                            scrolledContainerColor = container
                        ),
                        scrollBehavior = scrollBehavior,
                        title = {
                            if (route == Screen.Settings.route || route == Screen.Map.route) {
                                Box(
                                    Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = { navController.navigateUp() },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null
                                        )
                                    }
                                    Text(if (route == Screen.Settings.route) "Settings" else "")
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Healixir",
                                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 45.sp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.offset(y = (-1).dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_apteka_tylko_miska),
                                        modifier = Modifier.size(40.dp),
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    )
                }
            }
        },

        bottomBar = {
            if (parentGraphRoute == "main_graph" || parentGraphRoute == "shopping_cart_graph") {
                Box(Modifier.animateContentSize()) {
                    AnimatedVisibility(
                        visible = showBottomBar,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(RoundedCornerShape(100.dp))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomAppBarButton(
                                    onClick = {
                                        if (route != Screen.HomeScreen.route) {
                                            navController.navigate(Screen.HomeScreen.route)
                                        }
                                    },
                                    icon = Icons.Default.Home,
                                    text = "Start"
                                )

                                Spacer(modifier = Modifier.width(20.dp))

                                BottomAppBarButton(
                                    onClick = {
                                        if (route != Screen.DrugSearchScreen.route) {
                                            navController.navigate(Screen.DrugSearchScreen.route())
                                        }
                                    },
                                    icon = Icons.Default.Medication,
                                    text = "Drugs"
                                )

                                Spacer(modifier = Modifier.width(20.dp))
                                BottomAppBarButton(
                                    onClick = {
                                        if (route != Screen.ListOfPharmacies.route) {
                                            navController.navigate(Screen.ListOfPharmacies.route)
                                        }
                                    },
                                    icon = Icons.Default.LocalHospital,
                                    text = "Pharmacies"
                                )

                            }
                        }
                    }
                }
            }
        },

        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        AppNavHost(
            nav = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}


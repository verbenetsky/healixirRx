package com.example.pharmacystore.ui.home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pharmacystore.common.DoubleBackReact
import com.example.pharmacystore.ui.theme.sagePerFirst
import com.example.pharmacystore.ui.theme.sagePerSecond

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToProfile: () -> Unit,
    navigateToDrugs: () -> Unit,
    navigateToShoppingCart: () -> Unit,
    navigateToPharmacies: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as Activity

//    LaunchedEffect(Unit) {
//        Log.d("NAV", "HOME prev = ${nav.previousBackStackEntry?.destination?.route}")
//    }

     DoubleBackReact(exit = { activity.finish() })

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // klucz: każdy tile dostaje weight, więc karty wypełniają pionowo ekran
        StartTile(
            title = "Profile",
            subtitle = "View & edit your details",
            icon = Icons.Outlined.Person,
            onClick = { navigateToProfile() },
            modifier = Modifier.weight(1f, fill = true)
        )

        StartTile(
            title = "Pharmacies near you",
            subtitle = "Find nearby pharmacies on the map",
            icon = Icons.Outlined.Map,
            onClick = { navigateToPharmacies() },
            modifier = Modifier.weight(1f, fill = true)
        )

        StartTile(
            title = "Search meds",
            subtitle = "Browse and add to your cart",
            icon = Icons.Outlined.Search,
            onClick = { navigateToDrugs() },
            modifier = Modifier.weight(1f, fill = true)
        )

        StartTile(
            title = "Cart",
            subtitle = "Review items and checkout",
            icon = Icons.Outlined.ShoppingCart,
            onClick = { navigateToShoppingCart() },
            modifier = Modifier.weight(1f, fill = true)
        )
    }
}

@Composable
private fun StartTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            // min wysokość gdy ekran jest niski; przy weight i tak urośnie
            .heightIn(min = 75.dp),
        shape = RoundedCornerShape(50.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // większy „badge” na ikonę
            Box(
                Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(sagePerFirst),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = sagePerSecond,
                    modifier = Modifier.size(60.dp)
                )
            }

            // teksty rosną i łamią się, żeby wyglądało jak w mockupie
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

        }
    }
}

package com.example.pharmacystore.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import com.example.pharmacystore.ui.theme.surfaceDark

// odpowiada to za gradient na status bar, bez tego nie dziala nic
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AppGradientRoot(graph: NavGraphs?, route: String?, content: @Composable () -> Unit) {


    Box(Modifier.fillMaxSize()) {

        // 1) pełnoekranowe tło – widać je też POD statusem
        val bg: Brush =
            if (graph == NavGraphs.MAIN_GRAPH || graph == NavGraphs.SHOPPING_CART_GRAPH && route != Screen.SummaryCheckoutScreen.route) SolidColor(
                surfaceDark
            ) else returnGradientBackGround()

        Box(
            modifier = Modifier
                .background(bg)
                .fillMaxSize()
        )

        // 2) właściwa treść, odsunięta od ikon status bara
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            content()
        }
    }
}

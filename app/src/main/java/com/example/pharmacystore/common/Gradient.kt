package com.example.pharmacystore.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


//@Composable
//fun returnGradientBackGround(): Brush {
//    val gradient = if (isSystemInDarkTheme()) {
//        Brush.linearGradient(
//            0.00f to Color(0xFFCCCCB2),            // jasny beż
//            0.50f to Color(0xFF9EA068),            // pośredni odcień
//            1.00f to Color(0xFF757519),            // ciemniejszy oliwkowy
//            start = Offset(0f, 0f),
//            end   = Offset(700f, 0f)
//        )
//    } else {
//        Brush.linearGradient(
//            colorStops = arrayOf(
//                0.00f to Color.White.copy(alpha = 0.9f),                         // 0% – lekko przygaszona biel
//                0.40f to MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),  // od 35% – bardzo subtelny akcent głównego koloru
//                0.60f to MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),  // do 65%
//                1.00f to Color.White.copy(alpha = 0.4f)                          // 100% – znowu lekko przygaszona biel
//            ),
//            start = Offset(0f, 0f),
//            end = Offset(700f, 300f)  // poziomo na szerokość ~700px, możesz tu też użyć dynamicznego size.width
//        )
//    }
//    return gradient
//}

@Composable
fun returnGradientBackGround(): Brush {
    val gradient =
        Brush.linearGradient(
            0.00f to Color(0xFFCCCCB2),            // jasny beż
            0.50f to Color(0xFF9EA068),            // pośredni odcień
            1.00f to Color(0xFF757519),            // ciemniejszy oliwkowy
            start = Offset(0f, 0f),
            end = Offset(700f, 0f)
        )

    return gradient
}








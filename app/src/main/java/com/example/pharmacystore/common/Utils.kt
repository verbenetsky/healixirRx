package com.example.pharmacystore.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pharmacystore.ui.theme.darkScheme
import com.example.pharmacystore.ui.theme.sagePerFirst
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.floor


fun convertWojNumberToWojString(wojNumber: String): String {
    return when (wojNumber) {
        "2" -> "woj. dolnośląskie"
        "4" -> "woj. kujawsko-pomorskie"
        "6" -> "woj. lubelskie"
        "8" -> "woj. lubuskie"

        "10" -> "woj. łódzkie"
        "12" -> "woj. małopolskie"
        "14" -> "woj. mazowieckie"
        "16" -> "woj. opolskie"

        "18" -> "woj. podkarpackie"
        "20" -> "woj. podlaskie"
        "22" -> "woj. pomorskie"
        "24" -> "woj. śląskie"

        "26" -> "woj. świętokrzyskie"
        "28" -> "woj. warmińsko-mazurskie"
        "30" -> "woj. wielkopolskie"
        "32" -> "woj. zachodniopomorskie"

        else -> "Error"
    }
}

@Composable
fun DoubleBackReact(
    enabled: Boolean = true,
    message: String = "Press back again to exit",
    exit: () -> Unit
) {
    if (!enabled) return

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var backOnce by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (backOnce) {
            exit()                         // np. activity.finish()
        } else {
            backOnce = true
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()

            scope.launch {
                delay(2_000)
                backOnce = false           // reset po 2 s
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomAppBarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = sagePerFirst.copy(0.12f),   //  kolor tła
            contentColor = sagePerFirst      // kolor ikony
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
            )
            Text(text, fontSize = 10.sp)
        }
    }
}


fun millisToDateString(
    millis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter
        .ofPattern("dd.MM.yyyy", locale)       // jawny locale
    val date: LocalDate = Instant.ofEpochMilli(millis)
        .atZone(zone)
        .toLocalDate()                         // ↩ tylko data, bez czasu
    return date.format(formatter)
}

fun isEmail(data: String): String {
    return if (data.contains("@"))
        "email"
    else
        "phoneNumber"
}

fun combineAddress(
    city: String,
    woj: String,
    powiat: String,
    street: String,
    houseNumber: String
): String {
    val streetBlock = when {
        street.isNotBlank() && houseNumber.isNotBlank() -> "$street $houseNumber"
        street.isNotBlank() -> street
        houseNumber.isNotBlank() -> houseNumber
        else -> ""
    }

    return listOfNotNull(
        streetBlock.takeIf { it.isNotBlank() },
        city.takeIf { it.isNotBlank() },
        powiat.takeIf { it.isNotBlank() }?.let { "powiat $it" },
        woj.lowercase().replaceFirstChar { it.titlecase() }.takeIf { it.isNotBlank() }
    ).joinToString(", ")
}

fun Throwable.toMessage(): String = when (this) {
    is java.net.ConnectException,
    is java.net.UnknownHostException -> "No internet connection"

    is java.net.SocketTimeoutException -> "Request timed out"
    is retrofit2.HttpException -> when (code()) {
        401, 403 -> "Unauthorized"
        404 -> "Not found"
        in 500..599 -> "Server error"
        else -> "Request failed (${code()})"
    }

    else -> localizedMessage?.takeIf { it.isNotBlank() } ?: "Something went wrong"
}


val emailPattern: Pattern = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun validateEmail(email: String): Boolean {
    return emailPattern.matcher(email).matches()
}


fun truncateTo2Decimals(value: Double): Double {
    return floor(value * 100) / 100.0
}


@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun GoToTop(
    modifier: Modifier = Modifier,
    goToTop: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier
            .padding(16.dp)
            .size(50.dp),
        onClick = goToTop,
    ) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "go to top"
        )
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ForcedDarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}



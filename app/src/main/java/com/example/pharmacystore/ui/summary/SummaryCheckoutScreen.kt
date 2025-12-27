package com.example.pharmacystore.ui.summary

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.domain.model.UserInformationModel
import com.example.pharmacystore.ui.orders.OrdersViewModel
import kotlinx.coroutines.flow.SharedFlow

enum class PaymentMethod(val label: String) {
    BLIK("BLIK"),
    PAYPAL("PayPal"),
    PRZELEWY24("Przelewy24"),
    GOOGLE_PAY("Google Pay"),
    APPLE_PAY("Apple Pay"),
}

data class DeliveryInfo(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val streetAndNumber: String = "",
    val city: String = "",
    val postalCode: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCheckoutScreen(
    modifier: Modifier = Modifier,
    userData: UserInformationModel?,
    navigateToShoppingCart: () -> Unit,
    totalPrice: Double,
    state: OrdersViewModel.PlacingOrderState,
    events: SharedFlow<OrdersViewModel.OrderEvent>,
    onSuccessDialogConfirmed: () -> Unit,
    itemsCount: Int,
    onSubmitOrder: (DeliveryInfo, PaymentMethod) -> Unit,
) {
    if (userData == null) return

    val context = LocalContext.current
    var dialog by remember { mutableStateOf(false) }

    // --- Delivery form state (UI-only) ---
    var fullName by rememberSaveable { mutableStateOf(userData.nameSurname) }
    var phone by rememberSaveable { mutableStateOf(userData.phoneNumber) }
    var email by rememberSaveable { mutableStateOf(userData.email) }
    var streetAndNumber by rememberSaveable { mutableStateOf("${userData.street} ${userData.houseNumber}") }
    var city by rememberSaveable { mutableStateOf(userData.city) }
    var postalCode by rememberSaveable { mutableStateOf("") }

    var paymentMethod by rememberSaveable { mutableStateOf<PaymentMethod?>(null) }

    val canSubmit = remember(
        fullName, phone, streetAndNumber, city, postalCode, paymentMethod, email
    ) {
        fullName.isNotBlank()
                && streetAndNumber.isNotBlank()
                && city.isNotBlank()
                && postalCode.isNotBlank()
                && paymentMethod != null
                && !phone.isNullOrBlank()
                && !email.isNullOrEmpty()
    }

    LaunchedEffect(Unit) {
        events.collect { data ->
            when (data) {
                is OrdersViewModel.OrderEvent.ShowToastError -> {
                    Toast.makeText(context, data.msg, Toast.LENGTH_SHORT).show()
                }

                is OrdersViewModel.OrderEvent.ShowToastMess -> {
                    Toast.makeText(context, data.msg, Toast.LENGTH_SHORT).show()
                }

                OrdersViewModel.OrderEvent.OpenDialog -> {
                    dialog = true
                }

                OrdersViewModel.OrderEvent.NavigateToShoppingCart -> {
                    dialog = false
                    navigateToShoppingCart()
                }
            }
        }
    }

    if (state is OrdersViewModel.PlacingOrderState.Loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(returnGradientBackGround())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OrderSummaryCard(
                    itemsCount = itemsCount,
                    totalPrice = totalPrice,
                )

                SectionCard(title = "Delivery details") {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = phone ?: "",
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = email ?: "",
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = streetAndNumber,
                        onValueChange = { streetAndNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Street and number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("City") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Postal code") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next
                            )
                        )
                    }


                }

                SectionCard(title = "Payment method") {
                    PaymentMethod.entries.forEach { method ->
                        PaymentOptionRow(
                            method = method,
                            selected = paymentMethod == method,
                            onSelect = { paymentMethod = method }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        onClick = {
                            val info = DeliveryInfo(
                                fullName = fullName.trim(),
                                phone = phone?.trim() ?: "",
                                email = email?.trim() ?: "",
                                streetAndNumber = streetAndNumber.trim(),
                                city = city.trim(),
                                postalCode = postalCode.trim()
                            )
                            onSubmitOrder(info, paymentMethod!!)
                        },
                        enabled = canSubmit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        color = if (canSubmit) MaterialTheme.colorScheme.primary.copy(alpha = 0.95f) else MaterialTheme.colorScheme.primary.copy(
                            0.0f
                        ),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Place order",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }

    if (dialog) {
        PurchaseSuccessDialog {
            onSuccessDialogConfirmed()
        }
    }

}

@Composable
private fun OrderSummaryCard(
    itemsCount: Int,
    totalPrice: Double,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Items: $itemsCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = ink.copy(alpha = 0.75f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = ink.copy(alpha = 0.75f)
                )
                Text(
                    text = "${String.format("%.2f", totalPrice)} PLN",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val ink = MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        border = BorderStroke(0.4.dp, ink.copy(alpha = 0.10f)),
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                content()
            }
        )
    }
}

@Composable
private fun PaymentOptionRow(
    method: PaymentMethod,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val icon = when (method) {
        PaymentMethod.BLIK -> Icons.Default.Payments
        PaymentMethod.PAYPAL -> Icons.Default.CreditCard
        PaymentMethod.PRZELEWY24 -> Icons.Default.CreditCard
        PaymentMethod.GOOGLE_PAY -> Icons.Default.Payments
        PaymentMethod.APPLE_PAY -> Icons.Default.Payments
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            else ink.copy(alpha = 0.10f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                text = method.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            RadioButton(selected = selected, onClick = onSelect)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseSuccessDialog(
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onConfirm
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Purchase completed",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Your order has been placed successfully.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        onConfirm()
                    }) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}

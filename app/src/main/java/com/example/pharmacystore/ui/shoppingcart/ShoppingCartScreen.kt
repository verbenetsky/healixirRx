package com.example.pharmacystore.ui.shoppingcart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.domain.model.CartItem
import com.example.pharmacystore.remoteApi.BuyingInfo
import com.example.pharmacystore.remoteApi.UserBuyProductCheckInfo
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ShoppingCartScreen(
    modifier: Modifier = Modifier,
    state: ShoppingCartViewModel.ShoppingCartUiState,
    event: SharedFlow<ShoppingCartViewModel.ShoppingCartEvent>,
    items: List<CartItem>,
    totalCartPrice: Double,
    onCheckoutClick: (info: BuyingInfo) -> Unit,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onRemove: (packageNdc: String, pharmacyId: Int) -> Unit,
    navigateToPharmacyScreen: (pharmacyId: Int) -> Unit,
    navigateToDrugSearchScreen: (drugNdc: String) -> Unit,
    navigateToSummaryScreen: () -> Unit,
    changeStateToIdle: () -> Unit
) {
    val itemss = items.sortedBy { it.pharmacyId }
    val context = LocalContext.current

    var itemToRemove by remember { mutableStateOf<CartItem?>(null) }
    var dialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { changeStateToIdle() }
    }

    LaunchedEffect(Unit) {
        event.collect { value ->
            when (value) {
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastAdded -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }

                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastCoundNotVerifyStock -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }

                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastQtIsTooBig -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }

                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastThereIsNotEnoughItemsInStock -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }

                ShoppingCartViewModel.ShoppingCartEvent.NavigateToSummaryScreen -> {
                    navigateToSummaryScreen()
                }
            }
        }
    }

    when (state) {
        is ShoppingCartViewModel.ShoppingCartUiState.CannotBuy -> {
            val s = state.adjustments
        }

        ShoppingCartViewModel.ShoppingCartUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ShoppingCartViewModel.ShoppingCartUiState.Success -> {

        }

        ShoppingCartViewModel.ShoppingCartUiState.Idle -> {
            if (items.isEmpty()) {
                // Pusty koszyk
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Shopping cart",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = itemss,
                            key = { _, stock -> stock.packageNdc + "-" + stock.pharmacyId },
                            // samo packageNdc nie jest unikatowa wartoscia, dlatego dodajemy tez pharmacyId
                        ) { index, item ->

                            val showPharmacyHeader =
                                index == 0 || item.pharmacyId != itemss[index - 1].pharmacyId

                            if (showPharmacyHeader) {
                                PharmacyInfoCard(
                                    item = item,
                                    navigateToPharmacyScreen = { id -> navigateToPharmacyScreen(id) },
                                )
                            }

                            CartItemRow(
                                item = item,
                                onIncrease = { onIncrease(item) },
                                onDecrease = { onDecrease(item) },
                                onRemove = {
                                    dialog = true
                                    itemToRemove = item
                                },
                                navigateToDrugSearchScreen = { ndc -> navigateToDrugSearchScreen(ndc) },
                                showHeader = showPharmacyHeader,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Pasek podsumowania
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Items",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                                        )
                                        Text(
                                            text = "${items.sumOf { it.quantity }}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Unique",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                                        )
                                        Text(
                                            text = "${items.size}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                                        )
                                        Text(
                                            text = "${String.format("%.2f", totalCartPrice)} zł",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Button(
                                onClick = { onCheckoutClick(items.toBuyingInfo()) },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                            ) {
                                Text("Checkout")
                            }
                        }
                    }
                }
            }
        }

        is ShoppingCartViewModel.ShoppingCartUiState.Error -> Unit
    }



    if (dialog && itemToRemove != null) {
        RemoveFromCartDialog(
            onConfirm = { onRemove(itemToRemove!!.packageNdc, itemToRemove!!.pharmacyId) },
            onDismiss = { dialog = false }
        )
    }
}

@Composable
fun PharmacyInfoCard(item: CartItem, navigateToPharmacyScreen: (pharmacyId: Int) -> Unit) {
    // GÓRNA KARTA – APTEKA
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .clickable {
                navigateToPharmacyScreen(item.pharmacyId)
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.pharmacyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val addressLine = buildString {
                    item.address?.takeIf { it.isNotBlank() }?.let {
                        append(it)
                        append(", ")
                    }
                    append(item.city)
                }

                Text(
                    text = addressLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    navigateToDrugSearchScreen: (drugNdc: String) -> Unit,
    showHeader: Boolean,
    modifier: Modifier = Modifier
) {

    val dp = if (showHeader) 12.dp else 3.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dp, bottom = 1.dp)
    ) {
        // DOLNA KARTA – LEK
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
                .clickable {
                    navigateToDrugSearchScreen(item.drugNDC)
                }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    item.brandName
                        ?.takeIf { it.isNotBlank() }
                        ?.let { brand ->
                            Text(
                                text = brand,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    item.drugPackageDesc
                        .takeIf { it.isNotBlank() }
                        ?.let { desc ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "NDC: ${item.drugNDC}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (item.drugNDC != item.packageNdc) {
                        Text(
                            text = "Package NDC: ${item.packageNdc}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = "Labeler: ${item.labelerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // PRAWA KOLUMNA – ilość + usuń
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDecrease) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease quantity"
                            )
                        }

                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.widthIn(min = 24.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(onClick = onIncrease) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase quantity"
                            )
                        }
                    }

                    val str = String.format("%.2f", (item.price * item.quantity))

                    Text("$str zł", modifier = Modifier.padding(2.dp))

                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove from cart"
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveFromCartDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    BasicAlertDialog(
        onDismissRequest = onDismiss
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
                    text = "Remove item",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Are you sure you want to remove this item from the cart?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        onConfirm()
                        onDismiss()
                    }) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}

private fun List<CartItem>.toBuyingInfo(): BuyingInfo {
    val list = mutableListOf<UserBuyProductCheckInfo>()
    this.forEach { item ->
        list.add(
            UserBuyProductCheckInfo(
                pharmacyId = item.pharmacyId,
                packageNdc = item.packageNdc,
                qt = item.quantity
            )
        )
    }
    return BuyingInfo(list)
}

package com.example.pharmacystore.ui.orders

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pharmacystore.data.repository.OrderDetails
import com.example.pharmacystore.repo.OrderItem
import com.example.pharmacystore.ui.summary.PaymentMethod

@Composable
fun OrdersScreen(
    orders: List<OrderDetails>,
    loadOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        loadOrders()
    }

    val totalOrders = remember(orders) { orders.size }
    val totalItems = remember(orders) { orders.sumOf { it.orderItem.sumOf { i -> i.quantity } } }
    val totalSpent = remember(orders) { orders.sumOf { it.totalPrice } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Orders", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "No orders yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(
                items = orders,
                key = { o -> "${o.createdAt?.seconds ?: 0}_${o.totalPrice}_${o.status}" }
            ) { order ->
                OrderDetailsCard(order = order)
            }
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Orders: $totalOrders • Items: $totalItems",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "$totalSpent zł",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsCard(order: OrderDetails) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Status: ${order.status}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${formatTimestamp(order.createdAt)} • ${order.payMeth.pretty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${order.totalPrice} zł",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.orderItem.forEach { item ->
                    OrderLineItem(item = item)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Delivery (krótki snapshot)
            Text(
                text = "Delivery: ${order.deliveryData.fullName}, ${order.deliveryData.streetAndNumber}, ${order.deliveryData.city} ${order.deliveryData.postalCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OrderLineItem(item: OrderItem) {
    val title = item.name.takeIf { it.isNotBlank() } ?: item.packageNdc
    val lineTotal = remember(item.unitPrice, item.quantity) { item.unitPrice * item.quantity }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Pharmacy: ${item.pharmacyId} • NDC: ${item.packageNdc}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = "x${item.quantity}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$lineTotal zł",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}



private fun formatTimestamp(ts: com.google.firebase.Timestamp?): String {
    if (ts == null) return "Unknown date"
    val date = ts.toDate()
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return fmt.format(date)
}

private fun PaymentMethod.pretty(): String =
    this.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

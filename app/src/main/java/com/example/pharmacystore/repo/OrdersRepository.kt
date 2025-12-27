package com.example.pharmacystore.repo

import com.example.pharmacystore.data.repository.OrderDetails
import com.example.pharmacystore.domain.model.CartItem
import com.example.pharmacystore.remoteApi.OrderItemsInfo
import com.example.pharmacystore.remoteApi.StockDecrementItem
import com.example.pharmacystore.ui.summary.DeliveryInfo
import com.example.pharmacystore.ui.summary.PaymentMethod

interface OrdersRepository {

    suspend fun saveOrderToFirestore(
        payMeth: PaymentMethod,
        orderItem: List<OrderItem>,
        deliveryData: DeliveryInfo,
        totalPrice: Double
    ): Result<Unit>

    suspend fun updateQtOnServer(orderItems: OrderItemsInfo): Result<Unit>

    suspend fun getAllOrdersForUser(): Result<List<OrderDetails>>
}

data class OrderItem(
    val pharmacyId: Int = 0,
    val packageNdc: String = "",
    val name: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 0
)

fun List<CartItem>.toStockDecrementItem() : List<StockDecrementItem> {
    val lst = mutableListOf<StockDecrementItem>()
    this.forEach { item ->
        lst.add(StockDecrementItem(
            pharmacyId = item.pharmacyId,
            packageNdc = item.packageNdc,
            quantity = item.quantity
        ))
    }
    return lst
}

fun List<CartItem>.toOrderItem() : List<OrderItem> {
    val lst = mutableListOf<OrderItem>()
    this.forEach { item ->
        lst.add(OrderItem(
            pharmacyId = item.pharmacyId,
            packageNdc = item.packageNdc,
            name = item.name,
            unitPrice = item.price,
            quantity = item.quantity
        ))
    }

    return lst
}


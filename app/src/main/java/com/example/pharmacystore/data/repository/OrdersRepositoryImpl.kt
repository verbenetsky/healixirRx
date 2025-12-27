package com.example.pharmacystore.data.repository

import com.example.pharmacystore.remoteApi.DrugApi
import com.example.pharmacystore.remoteApi.OrderItemsInfo
import com.example.pharmacystore.repo.OrderItem
import com.example.pharmacystore.repo.OrdersRepository
import com.example.pharmacystore.ui.summary.DeliveryInfo
import com.example.pharmacystore.ui.summary.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class OrdersRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val drugApi: DrugApi
) : OrdersRepository {

    private val uid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Not logged In")


    override suspend fun getAllOrdersForUser(): Result<List<OrderDetails>> =
        runCatching {
            val snap = firestore.collection("users")
                .document(uid)
                .collection("orders")
                .get()
                .await()

            if (snap.isEmpty) {
                emptyList<OrderItem>()
                println("empty order")
            } else {
                println("not empty")
            }

            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(OrderDetails::class.java)
            }

            list
        }

    override suspend fun saveOrderToFirestore(
        payMeth: PaymentMethod,
        orderItem: List<OrderItem>,
        deliveryData: DeliveryInfo,
        totalPrice: Double
    ): Result<Unit> = runCatching {

        val docRef = firestore.collection("users")
            .document(uid)
            .collection("orders")
            .document()

        docRef.set(
            OrderDetails(
                "PLACED",
                totalPrice,
                payMeth,
                orderItem,
                deliveryData
            ), SetOptions.merge()
        ).await()

        docRef.update("createdAt", FieldValue.serverTimestamp())

        Result.success(Unit)
    }

    override suspend fun updateQtOnServer(orderItems: OrderItemsInfo): Result<Unit> = runCatching {
        val resp = drugApi.updateStockSize(orderItems)
        if (!resp.isSuccessful)
            throw retrofit2.HttpException(resp)
        resp.body() ?: throw IllegalStateException("empty body")
    }


}

data class OrderDetails(
    val status: String,
    val totalPrice: Double,
    val payMeth: PaymentMethod,
    val orderItem: List<OrderItem>,
    val deliveryData: DeliveryInfo,
    val createdAt: com.google.firebase.Timestamp? = null // będzie serverTimestamp
) {
    constructor() : this(
        status = "",
        totalPrice = 0.0,
        payMeth = PaymentMethod.BLIK, // albo PaymentMethod.UNKNOWN
        orderItem = emptyList(),
        deliveryData = DeliveryInfo(),
        createdAt = null
    )
}


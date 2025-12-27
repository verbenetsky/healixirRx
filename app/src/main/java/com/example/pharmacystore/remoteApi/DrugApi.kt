package com.example.pharmacystore.remoteApi

import com.example.pharmacystore.data.remote.MedStockDrugDto
import com.example.pharmacystore.data.remote.MedStockDto
import com.example.pharmacystore.repo.OrderItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface DrugApi {
    // zwraca dla konkretnego leku (drug ndc) liste aptek, cene i ilosc tego towartu w kazdej z aptek
    @GET("med_stock")
    suspend fun getMedStock(
        @Query("package_ndc") packageNdc: String,
        @Query("user_lat") userLat: Double,
        @Query("user_lon") userLon: Double
    ): List<MedStockDto>

    @GET("pharmacy_stock")
    suspend fun getPharmacyStock(
        @Query("pharmacy_id") pharmacyId: Int,
    ): List<MedStockDrugDto>

    // fun sprawdza dla pojedynczego rekorku w koszyku czy jest mozliwosc zakupu tego produktu w dokladnie tej ilosci

    @POST("validate_checkout")
    suspend fun checkIfUserCanBuyProduct(
        @Body info: BuyingInfo
    ): Response<CanBuyResponse>

    // fun zmniejsza ilosc kazdego produktu jaki kupil user updejtujac na serwerze tabele
    @POST("update_stock")
    suspend fun updateStockSize(
        @Body data: OrderItemsInfo
    ): Response<Unit>
}

data class OrderItemsInfo(
    val listOfItems: List<StockDecrementItem>
)

data class StockDecrementItem(
    val pharmacyId: Int = 0,
    val packageNdc: String = "",
    val quantity: Int = 0
)


data class BuyingInfo(
    val listOfUserBuyProductCheckInfo: List<UserBuyProductCheckInfo>
)

data class UserBuyProductCheckInfo(
    val pharmacyId: Int,
    val packageNdc: String,
    val qt: Int
)

data class CanBuyResponse(
    val ok: Boolean,
    val adjustments: List<ResponseAdjustments> = emptyList() // jesli empty to i ok musi byc true
)

data class ResponseAdjustments(
    val packageNdc: String,
    val requested: Int,
    val available: Int
)

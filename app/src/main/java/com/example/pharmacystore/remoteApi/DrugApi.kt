package com.example.pharmacystore.remoteApi

import com.example.pharmacystore.data.remote.MedStockDrugDto
import com.example.pharmacystore.data.remote.MedStockDto
import retrofit2.http.GET
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
}


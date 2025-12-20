package com.example.pharmacystore.remoteApi

import com.example.pharmacystore.data.remote.FullPharmacyDto
import com.example.pharmacystore.data.remote.MedStockDto
import com.example.pharmacystore.data.remote.PharmacyDto
import com.example.pharmacystore.ui.pharmacies.SortOption
import com.example.pharmacystore.ui.pharmacies.SortOrder
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PharmacyApi {
    // metoda odpowiadajaca za wyslanie wspolrzednych usera na serwer
    @POST("send_coordinates")
    suspend fun getNearbyPharmacies(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("sort_order") sortOrder: SortOrder,
        @Query("sorted_by") sortedBy: SortOption,
        @Query("is_open") isOpen: Boolean,
        @Body coordinatesData: CoordinatesDataDto
    ) :  List<PharmacyDto>

    @GET("pharmacies/{id}")
    suspend fun getPharmacyDetails(
        @Path("id") id: Int
    ) : FullPharmacyDto


    @GET("pharmacies/{pharmacy_id}/stock/{package_ndc}")
    suspend fun getMaxAvailableQuantity(
        @Path("pharmacy_id") pharmacyId: Int,
        @Path("package_ndc") packageNdc: String
    ) : Int
}

data class CoordinatesDataDto(
    val lat: Double,
    val lon: Double,
    val radiusMeters: Int
)


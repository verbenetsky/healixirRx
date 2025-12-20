package com.example.pharmacystore.remoteApi

import com.example.pharmacystore.domain.model.Drug
import com.example.pharmacystore.domain.model.UpcResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi  {

    // https://api.fda.gov/drug/ndc.json?search=brand_name%3A%ibuprofen%
    @GET("drug/ndc.json")
    suspend fun getDrugByName(
        @Query("search") search: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int = 30,
    ): DrugResponse

    // https://api.fda.gov/drug/ndc.json?search=openfda.upc.exact%3A0358151455010&limit=1
    // statystycznie tylko co 3-4 lek ma przypisany UPC wiec nie jest to fajnie zaimplementowane
    @GET("drug/ndc.json")
    suspend fun getDrugByUPC( // upc jest to barcode, wazne jest ze nie kazdy lek ma upc
        @Query("search") search: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int = 30,
    ): DrugResponse

    // https://api.fda.gov/drug/ndc.json?search=product_ndc:"50090-6051"
    // przykladowy linka dla getDrugByProductNDC
    @GET("drug/ndc.json")
    suspend fun getDrugByProductNDC(
        @Query("search") search: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int = 30,
    ): DrugResponse


    // wywolac jesli getDrugByProductNDC sie nie udalo
    @GET("drug/ndc.json")
    suspend fun getDrugByPackageNDC(
        @Query("search") search: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int = 30,
    ): DrugResponse

    // metoda ktora pozwala pobrac tylko i wylacznie upc codes (kode kreskowe dla konkretnego leku)
    @GET("drug/ndc.json")
    suspend fun getUpcCodeForDrugNdc(
        @Query("search") search: String,
    ): UpcResponseDto
}

data class DrugResponse(val results: List<Drug> = emptyList())



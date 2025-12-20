package com.example.pharmacystore.repo

import androidx.paging.PagingData
import com.example.pharmacystore.data.local.entities.DrugWithDetails
import com.example.pharmacystore.data.remote.MedStockDrugDto
import com.example.pharmacystore.data.remote.MedStockDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DrugRepository {

    val searchBarState: StateFlow<String>

    fun updateSearchBarState(value: String)

    fun paged(searchBarState: String): Flow<PagingData<DrugWithDetails>>

    suspend fun checkMedStock(
        packageNdc: String,
        usersLat: Double,
        usersLon: Double
    ): Result<List<MedStockDto>>

    suspend fun getPharmacyStock(pharmacyId: Int): Result<List<MedStockDrugDto>>

    suspend fun getUpcCodes(drugNdc: String): Result<List<String>>

//    suspend fun getDragByName(name: String): Result<List<Drug>?>
//
//    suspend fun getDragByUPC(upc: String): Result<List<Drug>?>
//
//    suspend fun getDragByPackageNDC(ndc: String): Result<List<Drug>?>
//
//    suspend fun getDragByProductNDC(ndc: String): Result<List<Drug>?>
}
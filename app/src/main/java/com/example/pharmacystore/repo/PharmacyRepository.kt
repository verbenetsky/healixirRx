package com.example.pharmacystore.repo

import androidx.compose.runtime.State
import androidx.paging.PagingData
import com.example.pharmacystore.data.local.entities.PharmacyEntity
import com.example.pharmacystore.data.remote.FullPharmacyDto
import com.example.pharmacystore.data.repository.GeoPoint
import com.example.pharmacystore.data.repository.PharmFilter
import com.example.pharmacystore.remoteApi.CoordinatesDataDto
import com.example.pharmacystore.ui.pharmacies.PharmacyLight
import com.example.pharmacystore.ui.pharmacies.SortOption
import com.example.pharmacystore.ui.pharmacies.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PharmacyRepository {

    val listOfPharmacies: StateFlow<List<PharmacyLight>?>

    val radius: StateFlow<Int>
    val sortOrder: StateFlow<SortOrder>

    val sortedBy: StateFlow<SortOption>

    val isOpen: StateFlow<Boolean>

    fun changeIsOpen(value: Boolean)

    fun changeRadius(radius: Int)
    fun changeSortOrder(order: SortOrder)

    fun changeSortedBy(value: SortOption)

    suspend fun getFullDetails(id: Int): Result<FullPharmacyDto>

    fun paged(filter: PharmFilter): Flow<PagingData<PharmacyEntity>>
}
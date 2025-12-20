package com.example.pharmacystore.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.pharmacystore.data.local.db.PharmacyDatabase
import com.example.pharmacystore.data.local.entities.PharmacyEntity
import com.example.pharmacystore.data.local.mediator.PharmaciesRemoteMediator
import com.example.pharmacystore.data.remote.FullPharmacyDto
import com.example.pharmacystore.remoteApi.PharmacyApi
import com.example.pharmacystore.repo.LocationRepository
import com.example.pharmacystore.repo.PharmacyRepository
import com.example.pharmacystore.ui.pharmacies.PharmacyLight
import com.example.pharmacystore.ui.pharmacies.SortOption
import com.example.pharmacystore.ui.pharmacies.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PharmacyRepositoryImpl @Inject constructor(
    private val api: PharmacyApi,
    private val db: PharmacyDatabase,
    private val locationRepo: LocationRepository,
) : PharmacyRepository {

    private val _listOfPharmacies = MutableStateFlow<List<PharmacyLight>?>(null)
    override val listOfPharmacies: StateFlow<List<PharmacyLight>?> = _listOfPharmacies.asStateFlow()

    private val _radius = MutableStateFlow(3)
    override val radius: StateFlow<Int> = _radius.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ASC)
    override val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _sortedBy = MutableStateFlow(SortOption.Distance)
    override val sortedBy: StateFlow<SortOption> = _sortedBy.asStateFlow()

    private val _isOpen = MutableStateFlow(false)
    override val isOpen = _isOpen.asStateFlow()

    override fun changeIsOpen(value: Boolean) {
        _isOpen.value = value
    }

    override fun changeSortedBy(value: SortOption) {
        _sortedBy.value = value
    }

    override fun changeRadius(radius: Int) {
        _radius.value = radius
    }

    override fun changeSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    override suspend fun getFullDetails(id: Int): Result<FullPharmacyDto> =
        runCatching { api.getPharmacyDetails(id) }


    // @formatter:off
    @OptIn(ExperimentalPagingApi::class)
    override fun paged(filter: PharmFilter): Flow<PagingData<PharmacyEntity>> {
        val mediator = PharmaciesRemoteMediator(api, db, this, locationRepo)
        println("pager pharmacy repository")
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            remoteMediator = mediator,
            pagingSourceFactory = { db.pharmacyDao.pagingSource() }
        ).flow
    }
}
// @formatter:on

data class PharmFilter(
    val isOpen: Boolean = false,         // false = wszystkie
    val asc: Boolean = true,
    val orderBy: SortOption = SortOption.Distance
)

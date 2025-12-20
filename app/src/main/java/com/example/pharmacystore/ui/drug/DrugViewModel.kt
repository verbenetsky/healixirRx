package com.example.pharmacystore.ui.drug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.pharmacystore.data.mappers.toDrug
import com.example.pharmacystore.data.remote.MedStock
import com.example.pharmacystore.data.remote.MedStockDrug
import com.example.pharmacystore.data.remote.toDrug
import com.example.pharmacystore.data.remote.toMedStock
import com.example.pharmacystore.data.repository.DrugRepositoryImpl
import com.example.pharmacystore.domain.model.Drug
import com.example.pharmacystore.repo.LocationRepository
import com.example.pharmacystore.ui.pharmacies.SortOption
import com.example.pharmacystore.ui.pharmacies.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DrugViewModel @Inject constructor(
    private val repo: DrugRepositoryImpl,
    private val mapRepo: LocationRepository
) : ViewModel() {

    private val _pharmacyStockState = MutableStateFlow<PharmacyStockState>(PharmacyStockState.Idle)
    val pharmacyStockState = _pharmacyStockState.asStateFlow()

    private val _medStockState = MutableStateFlow<MedStockState>(MedStockState.Idle)
    val medStockState = _medStockState.asStateFlow()

    private val _pickPackageState = MutableStateFlow<Drug?>(null)
    val pickPackageState = _pickPackageState.asStateFlow()

    fun pickPackage(drug: Drug) {
        _pickPackageState.value = drug
    }

    val searchBarState: StateFlow<String> = repo.searchBarState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    fun changeSearchBarState(value: String) {
        repo.updateSearchBarState(value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val drugsPagingFlow: Flow<PagingData<Drug>> =
        searchBarState
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    repo.paged(query)
                }
            }
            .map { pagingData -> pagingData.map { it.toDrug() } }
            .cachedIn(viewModelScope)


    fun getMedStock(
        packageNdc: String,
        sortOption: SortOption? = null,
        sortOrder: SortOrder? = null
    ) {

        if (mapRepo.location.value?.lat == null || mapRepo.location.value?.lon == null) {
            println("locaiton is null")
            return
        }

        viewModelScope.launch {
            _medStockState.value = MedStockState.Loading

            val res = repo.checkMedStock(
                packageNdc,
                usersLat = mapRepo.location.value!!.lat,
                usersLon = mapRepo.location.value!!.lon
            )

            res.onSuccess { x ->

                val baseList = x.map { it.toMedStock() }

                if (sortOption == null || sortOrder == null) {
                    _medStockState.value = MedStockState.Success(baseList)
                }

                val res = when (sortOption) {
                    SortOption.Distance -> {
                        when (sortOrder) {
                            SortOrder.ASC -> baseList.sortedBy { it.pharmacy.distanceKms }
                            SortOrder.DESC -> baseList.sortedByDescending { it.pharmacy.distanceKms }
                            null -> baseList
                        }
                    }

                    SortOption.Name -> {
                        when (sortOrder) {
                            SortOrder.ASC -> baseList.sortedBy { it.pharmacy.name }
                            SortOrder.DESC -> baseList.sortedByDescending { it.pharmacy.name }
                            null -> baseList
                        }
                    }

                    null -> baseList
                }

                _medStockState.value = MedStockState.Success(res)

                println("state:")
                println(_medStockState.value)
            }.onFailure { err ->
                _medStockState.value = MedStockState.Error(err.localizedMessage ?: "unknown error")
                println("state failure:")
                println(_medStockState.value)
            }
        }
    }

    fun getPharmacyStock(pharmacyId: Int) {
        viewModelScope.launch {
            _pharmacyStockState.value = PharmacyStockState.Loading

            val res = repo.getPharmacyStock(pharmacyId)

            res.onSuccess { it ->
                _pharmacyStockState.value = PharmacyStockState.Success(it.map { it.toDrug() })
                println("pharmacy stock:")
                println(_pharmacyStockState.value)
            }.onFailure { err ->
                _pharmacyStockState.value =
                    PharmacyStockState.Error(err.localizedMessage ?: "Unknown error")
                println("pharmacy stock:")
                println(_pharmacyStockState.value)
            }
        }
    }

    suspend fun getUpcCodes(productNdc: String): UpcCodesResult {
        val res = repo.getUpcCodes(productNdc)
        res.onSuccess { result ->
            return if (result.isEmpty()) {
                UpcCodesResult.NoData
            } else {
                UpcCodesResult.Success(result)
            }
        }.onFailure { msg ->
            return UpcCodesResult.Error(msg.localizedMessage ?: "Unknown error")
        }
        return UpcCodesResult.Error("Unknown error") // kod nie osiagalny ale ide krzyczy
    }


    sealed interface UpcCodesResult {
        data class Success(val codes: List<String>) : UpcCodesResult
        data object NoData : UpcCodesResult
        data class Error(val message: String) : UpcCodesResult
    }

    fun changeState(state: MedStockState) {
        _medStockState.value = state
    }

    sealed class MedStockState {
        data object Idle : MedStockState()
        data object Loading : MedStockState()
        data class Success(val listOfMedStock: List<MedStock>) : MedStockState()
        data class Error(val message: String) : MedStockState()
    }

    sealed class PharmacyStockState {
        data object Idle : PharmacyStockState()
        data object Loading : PharmacyStockState()
        data class Success(val listOfMedStockDrugs: List<MedStockDrug>) : PharmacyStockState()
        data class Error(val message: String) : PharmacyStockState()
    }
}


package com.example.pharmacystore.ui.pharmacies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.pharmacystore.common.toMessage
import com.example.pharmacystore.data.mappers.toPharmacy
import com.example.pharmacystore.data.remote.FullPharmacyDto
import com.example.pharmacystore.data.remote.PharmacyShort
import com.example.pharmacystore.data.repository.PharmFilter
import com.example.pharmacystore.domain.model.Packaging
import com.example.pharmacystore.repo.LocationRepository
import com.example.pharmacystore.repo.PharmacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val pharmacyRepository: PharmacyRepository,
) : ViewModel() {

    private val _hasUserSearched = MutableStateFlow(false)
    val hasUserSearched = _hasUserSearched.asStateFlow()

    private val _state = MutableStateFlow<DetailsState>(DetailsState.Idle)
    val state = _state.asStateFlow()

    private val _pharmacyInfoPharmacyStock = MutableStateFlow<PharmacyShort?>(null)
    val pharmacyInfoPharmacyStock = _pharmacyInfoPharmacyStock.asStateFlow()

    fun savePharmacyInfo(info: PharmacyShort) {
        _pharmacyInfoPharmacyStock.value = info
    }

    private val _pharmacyDetails = MutableStateFlow<DetailsState>(DetailsState.Idle)
    val pharmacyDetails = _pharmacyDetails.asStateFlow()

    fun getPharmacyDetails(id: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _pharmacyDetails.value = DetailsState.Loading
            val res = pharmacyRepository.getFullDetails(id)
            res.onSuccess { info ->
                _pharmacyDetails.value = DetailsState.Success(info)
                onSuccess()
            }.onFailure { e ->
                _pharmacyDetails.value = DetailsState.Error(e.toMessage())
            }
        }
    }

    fun changeRadius(newRadius: Int) = pharmacyRepository.changeRadius(newRadius)

    fun changeSortOrder(order: SortOrder) = pharmacyRepository.changeSortOrder(order)

    fun changeSortedBy(value: SortOption) = pharmacyRepository.changeSortedBy(value)

    fun changeIsOpen(value: Boolean) = pharmacyRepository.changeIsOpen(value)


    val radius: StateFlow<Int> =
        pharmacyRepository.radius
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 3
            )


    val sortOrder: StateFlow<SortOrder> =
        pharmacyRepository.sortOrder
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SortOrder.ASC
            )

    val sortedBy: StateFlow<SortOption> =
        pharmacyRepository.sortedBy
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SortOption.Distance
            )


    val isOpen: StateFlow<Boolean> =
        pharmacyRepository.isOpen
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    private val appliedFilter = MutableStateFlow(
        PharmFilter(
            isOpen = isOpen.value,
            asc = (sortOrder.value == SortOrder.ASC),
            orderBy = sortedBy.value
        )
    )

    fun applyFilters() {
        _hasUserSearched.value = true
        appliedFilter.value = PharmFilter(
            isOpen = isOpen.value,
            asc = (sortOrder.value == SortOrder.ASC),
            orderBy = sortedBy.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmaciesPagingFlow =
        appliedFilter
            .flatMapLatest { f -> pharmacyRepository.paged(f) }     // ← za każdą zmianą buduje się nowy Pager
            .map { it.map { e -> e.toPharmacy() } }
            .cachedIn(viewModelScope)

    fun ensureLocationFromAddressIfNeeded(address: String) = viewModelScope.launch {
        if (locationRepository.location.value == null && address.isNotBlank()) {
            locationRepository.geocode(address)
        }
    }

    sealed interface DetailsState {
        data class Success(val pharmacy: FullPharmacyDto) : DetailsState
        data object Loading : DetailsState
        data class Error(val msg: String) : DetailsState
        data object Idle : DetailsState
    }

    // ---------------------------indeksy listy-----------------------------------------------------
    var savedIndex by mutableIntStateOf(0) ; private set

    var savedOffset by mutableIntStateOf(0) ; private set

    fun saveIndex(index: Int, offset: Int) { savedIndex = index ; savedOffset = offset }
    //----------------------------------------------------------------------------------------------


    //----------------------------Pick Package Screen, Available package----------------------------
    private val _pickedPackage = MutableStateFlow<Packaging?>(null)
    val pickedPackage = _pickedPackage.asStateFlow()

    fun pickPackage(value: Packaging?) { _pickedPackage.value = value }

    //----------------------------------------------------------------------------------------------
}


data class PharmacyLight(
    val identyfikator_apteki: Int,
    val nazwa_apteki: String?,
    val rodzaj_apteki: String,
    val powiat: String,
    val gmina: String?,
    val typ_ulicy: String?,
    val ulica_znormalizowana: String,
    val miejscowosc: String?,
    val distance: Double,

    val lat: Double?,
    val lon: Double?,

    val isOpenNow: Boolean,
)
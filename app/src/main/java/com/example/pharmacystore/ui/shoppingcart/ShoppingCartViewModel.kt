package com.example.pharmacystore.ui.shoppingcart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.common.toMessage
import com.example.pharmacystore.domain.model.CartItem
import com.example.pharmacystore.remoteApi.BuyingInfo
import com.example.pharmacystore.remoteApi.ResponseAdjustments
import com.example.pharmacystore.repo.ShoppingCartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingCartViewModel @Inject constructor(private val repo: ShoppingCartRepository) :
    ViewModel() {

    private val _canIncreaseMap = MutableStateFlow<Map<CartKey, Boolean>>(emptyMap())
    val canIncreaseMap: StateFlow<Map<CartKey, Boolean>> = _canIncreaseMap

    val cartItems: StateFlow<List<CartItem>> =
        repo.observeCart()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val cartSize: StateFlow<Int> =
        repo.observeCartSize()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    private val _state = MutableStateFlow<ShoppingCartUiState>(ShoppingCartUiState.Idle)
    val state = _state.asStateFlow()

    fun changeState(state: ShoppingCartUiState) {
        _state.value = state
    }

    private val _events = MutableSharedFlow<ShoppingCartEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun addItemToCart(item: CartItem) {
        viewModelScope.launch {
            runCatching {
                repo.addItemToCart(item)
            }.onSuccess {
                if (item.quantity == 1) {
                    _events.tryEmit(ShoppingCartEvent.ShowToastAdded(msg = "Item added to cart"))
                } else {
                    _events.tryEmit(ShoppingCartEvent.ShowToastAdded(msg = "Items added to cart"))
                }
            }.onFailure { err ->
                _state.value = ShoppingCartUiState.Error(err.localizedMessage ?: "Error")
            }
        }
    }

    fun checkIfCanBuy(info: BuyingInfo) {
        viewModelScope.launch {
            _state.value = ShoppingCartUiState.Loading
            val res = repo.checkIfCanBuy(info)
            res.onSuccess { response ->
                if (response.ok) { // nie ma zadnych adjustmenst
                    _events.tryEmit(ShoppingCartEvent.NavigateToSummaryScreen)
                    println("no adjustments needed")
                } else { // czegos brakuje na stanie
                    _state.value = ShoppingCartUiState.CannotBuy(response.adjustments)
                    println("some adjustments needed")
                }
            }.onFailure { err ->
                _state.value = ShoppingCartUiState.Error(err.toMessage())
                println("error")
            }
        }
    }

    val totalCartPrice: StateFlow<Double> =
        repo.observeTotalCartPrice()
            .stateIn(
                scope = viewModelScope,
                initialValue = 0.0,
                started = SharingStarted.WhileSubscribed(5_000)
            )

    fun increaseQt(item: CartItem) {
        viewModelScope.launch {
            println("zwieksza sie ilosc")
            repo.increaseQt(item)
        }
    }

    fun getMaxAvailableQuantity(
        packageNdc: String,
        pharmacyId: Int,
        usersQt: Int,
        onSuccess: () -> Unit
    ) { // zwraca ile maksymalnie moze byc konkretnego leku w konkretnej aptece
        viewModelScope.launch {
            val s = repo.getMaxAvailableQuantity(pharmacyId, packageNdc)
            s.onSuccess { maxQt ->
                println("max qt is $maxQt")
                println("user qt is $usersQt")
                if (usersQt < maxQt) {
                    onSuccess()
                } else {
                    _events.tryEmit(ShoppingCartEvent.ShowToastThereIsNotEnoughItemsInStock("You’ve reached the maximum quantity for this item."))
                }
            }.onFailure { err ->
                _events.tryEmit(
                    ShoppingCartEvent.ShowToastCoundNotVerifyStock(err.toMessage())
                )
                println(err)
            }
        }
    }


    fun decreaseQt(item: CartItem) = viewModelScope.launch { repo.decreaseQt(item) }

    fun checkIfCanIncreaseQt(
        packageNdc: String,
        pharmacyId: Int,
        usersQt: Int,
        stockSize: Int
    ) {
        println("packageNdc: $packageNdc")
        println("pharmacyId: $pharmacyId")
        println("usersQt: $usersQt")
        println("stockSize: $stockSize")

        viewModelScope.launch {
            val cartSize = repo.howManyParticularItemsInCart(packageNdc, pharmacyId)
            println("cartSize: $cartSize")

            if (cartSize + usersQt < stockSize) {
                _canIncreaseMap.update { old -> old + (CartKey(packageNdc, pharmacyId) to true) }
                // old stara mapa
                // + (CartKey(packageNdc, pharmacyId) to true) - nadpisuje lub dodaje nowy rekord do mapy z danymi (CartKey(packageNdc, pharmacyId) to true)
                println("can increase")
            } else {
                _canIncreaseMap.update { old ->
                    old + (CartKey(packageNdc, pharmacyId) to false)
                }
                println("can not increase")
            }
        }
    }

    fun checkIfCanAdd(
        packageNdc: String,
        pharmacyId: Int,
        usersQt: Int,
        stockSize: Int,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val cartSize = repo.howManyParticularItemsInCart(packageNdc, pharmacyId)

            if (cartSize + usersQt <= stockSize) {
                onSuccess()
            } else {
                _events.tryEmit(ShoppingCartEvent.ShowToastAdded(msg = "You’ve reached the maximum quantity for this item."))
                println("You’ve reached the maximum quantity for this item.")
            }
        }
    }

    fun onDecreaseMap(item: CartKey) {
        _canIncreaseMap.update { old -> old + (CartKey(item.packageNdc, item.pharmacyId) to true) }
    }

    fun removeItem(ndc: String, pharmacyId: Int) {
        viewModelScope.launch {
            runCatching {
                repo.removeItem(ndc, pharmacyId)
            }.onSuccess {
                _events.tryEmit(ShoppingCartEvent.ShowToastAdded(msg = "Item removed"))
            }.onFailure { err ->
                _state.value = ShoppingCartUiState.Error(err.localizedMessage ?: "Error")
            }
        }
    }

//    fun clearCart() {
//        viewModelScope.launch {
//            runCatching {
//                repo.clear()
//            }.onSuccess {
//                _events.tryEmit(ShoppingCartEvent.ShowToastAdded(msg = "Cart cleared"))
//            }.onFailure { err ->
//                _state.value = ShoppingCartUiState.Error(err.localizedMessage ?: "Error")
//            }
//        }
//    }

    sealed interface ShoppingCartUiState {
        data object Loading : ShoppingCartUiState
        data class Error(val err: String) : ShoppingCartUiState
        data object Idle : ShoppingCartUiState

        // ---------- purchase verification -------------
        data object Success :
            ShoppingCartUiState // jesli po sprawdzeniu wszytkie towary sa dostepne

        data class CannotBuy(val adjustments: List<ResponseAdjustments>) : ShoppingCartUiState
    }

    sealed interface ShoppingCartEvent {
        data object NavigateToSummaryScreen: ShoppingCartEvent
        data class ShowToastAdded(val msg: String) : ShoppingCartEvent
        data class ShowToastQtIsTooBig(val msg: String) : ShoppingCartEvent
        data class ShowToastCoundNotVerifyStock(val msg: String) : ShoppingCartEvent
        data class ShowToastThereIsNotEnoughItemsInStock(val msg: String) : ShoppingCartEvent
    }
}

data class CartKey(
    val packageNdc: String,
    val pharmacyId: Int
)
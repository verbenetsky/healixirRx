package com.example.pharmacystore.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.common.toMessage
import com.example.pharmacystore.data.repository.OrderDetails
import com.example.pharmacystore.remoteApi.OrderItemsInfo
import com.example.pharmacystore.remoteApi.StockDecrementItem
import com.example.pharmacystore.repo.OrderItem
import com.example.pharmacystore.repo.OrdersRepository
import com.example.pharmacystore.repo.ShoppingCartRepository
import com.example.pharmacystore.ui.summary.DeliveryInfo
import com.example.pharmacystore.ui.summary.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val ordersRepo: OrdersRepository,
    private val cartRepository: ShoppingCartRepository
) : ViewModel() {
    private val _orders: MutableStateFlow<List<OrderDetails>> = MutableStateFlow(emptyList())
    val orders = _orders.asStateFlow()

    private val _state = MutableStateFlow<PlacingOrderState>(PlacingOrderState.Idle)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OrderEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun loadOrders() {
        viewModelScope.launch {
            val res = ordersRepo.getAllOrdersForUser()
            res.onSuccess { value ->
                _orders.value = value
            }.onFailure { e ->
                println(e.localizedMessage ?: "error")
            }
        }
    }

    fun placeOrder(
        stockDecrementItems: List<StockDecrementItem>,
        paymentMethod: PaymentMethod,
        orderItem: List<OrderItem>,
        deliveryData: DeliveryInfo,
        totalPrice: Double
    ) {
        viewModelScope.launch {
            if (_state.value is PlacingOrderState.Loading) return@launch
            _state.value = PlacingOrderState.Loading

            try {
                // 1) update stock na serwerze (źródło prawdy dla magazynu)
                ordersRepo.updateQtOnServer(OrderItemsInfo(stockDecrementItems))

                // 2) zapis zamówienia do Firestore (na razie best-effort; docelowo outbox)
                ordersRepo.saveOrderToFirestore(paymentMethod, orderItem, deliveryData, totalPrice)

                // 3) wyczyść koszyk dopiero po sukcesie powyższych kroków
                cartRepository.clearCartForOneUser()

                _state.value = PlacingOrderState.Idle
                _events.emit(OrderEvent.OpenDialog)
                _events.emit(OrderEvent.ShowToastMess("You've successfully purchased"))
            } catch (e: CancellationException) {
                _state.value = PlacingOrderState.Idle
                throw e
            } catch (t: Throwable) {
                _events.emit(OrderEvent.ShowToastError(t.toMessage()))
            }
        }
    }

    fun onSuccessDialogConfirmed() {
        _events.tryEmit(OrderEvent.NavigateToShoppingCart)
    }

    sealed interface OrderEvent {
        data class ShowToastMess(val msg: String) : OrderEvent
        data class ShowToastError(val msg: String) : OrderEvent
        data object OpenDialog : OrderEvent
        data object NavigateToShoppingCart : OrderEvent
    }

    sealed interface PlacingOrderState {
        data object Idle : PlacingOrderState
        data object Loading : PlacingOrderState
    }
}
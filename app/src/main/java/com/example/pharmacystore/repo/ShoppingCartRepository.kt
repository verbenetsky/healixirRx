package com.example.pharmacystore.repo

import com.example.pharmacystore.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface ShoppingCartRepository {

    // sprawdza ile jest sztuk konkretnego leku dodano do koszyka
    suspend fun howManyParticularItemsInCart(packageNdc: String, pharmacyId: Int): Int // zwraca ilosc tego towaru w koszuku

    suspend fun getMaxAvailableQuantity(pharmacyId: Int, packageNdc: String): Result<Int>

    suspend fun addItemToCart(item: CartItem)

    suspend fun decreaseQt(item: CartItem)
    suspend fun increaseQt(item: CartItem)

    suspend fun removeItem(ndc: String, pharmacyId: Int)

    suspend fun clear()

    fun observeCart(): Flow<List<CartItem>>

    fun observeCartSize(): Flow<Int>

}
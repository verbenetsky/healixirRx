package com.example.pharmacystore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pharmacystore.data.local.entities.ShoppingCartEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ShoppingCartDao {

    @Insert
    suspend fun addItemToCart(item: ShoppingCartEntity)

    @Query("DELETE FROM shopping_cart WHERE packageNDC = :itemNDC AND pharmacyId = :pharmacyId")
    suspend fun deleteItemFromCart(itemNDC: String, pharmacyId: Int)

    @Query("SELECT * FROM shopping_cart WHERE packageNDC = :ndc AND pharmacyId = :pharmacyId")
    suspend fun getDrugByPackageNdcAndPharmacyId(ndc: String, pharmacyId: Int): ShoppingCartEntity?

    @Query("UPDATE shopping_cart SET quantity = :quantity WHERE packageNDC = :ndc AND pharmacyId = :pharmacyId")
    suspend fun updateQuantity(quantity: Int, ndc: String, pharmacyId: Int)

    @Query("SELECT quantity FROM shopping_cart WHERE packageNDC = :packageNdc AND pharmacyId = :pharmacyId")
    suspend fun checkIfCanIncreaseQt(packageNdc: String, pharmacyId: Int): Int

    @Query("SELECT * FROM shopping_cart WHERE userUID = :userUID")
    fun observeAll(userUID: String): Flow<List<ShoppingCartEntity>>

    @Query("DELETE FROM shopping_cart")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM shopping_cart WHERE userUID = :userUID")
    fun getCartSize(userUID: String): Flow<Int>
}
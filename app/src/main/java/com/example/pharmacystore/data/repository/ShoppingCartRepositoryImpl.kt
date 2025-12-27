package com.example.pharmacystore.data.repository

import android.net.http.HttpException
import com.example.pharmacystore.data.local.db.ShoppingCartDatabase
import com.example.pharmacystore.domain.model.CartItem
import com.example.pharmacystore.domain.model.toDomain
import com.example.pharmacystore.domain.model.toEntity
import com.example.pharmacystore.remoteApi.BuyingInfo
import com.example.pharmacystore.remoteApi.CanBuyResponse
import com.example.pharmacystore.remoteApi.DrugApi
import com.example.pharmacystore.remoteApi.PharmacyApi
import com.example.pharmacystore.repo.ShoppingCartRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.emptyList

@Singleton
class ShoppingCartRepositoryImpl @Inject constructor(
    private val db: ShoppingCartDatabase,
    private val pharmacyApi: PharmacyApi,
    private val drugApi: DrugApi,
    private val auth: FirebaseAuth
) : ShoppingCartRepository {

    private val _totalCartPrice = MutableStateFlow(0.0)
    override val totalCartPrice = _totalCartPrice.asStateFlow()

    private val uid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Not logged In")

    override suspend fun howManyParticularItemsInCart(packageNdc: String, pharmacyId: Int): Int {
        val qt = db.shoppingCartDao.checkIfCanIncreaseQt(packageNdc, pharmacyId)
        return qt
    }

    override suspend fun getMaxAvailableQuantity(pharmacyId: Int, packageNdc: String): Result<Int> {
         return runCatching {
            pharmacyApi.getMaxAvailableQuantity(pharmacyId,packageNdc)
        }
    }

    override suspend fun addItemToCart(item: CartItem) {
        if (auth.currentUser?.uid == null) {
            return
        } else {
            val existingItem =
                db.shoppingCartDao.getDrugByPackageNdcAndPharmacyId(item.packageNdc, item.pharmacyId)
            // jesli nie ma takiego produktu w koszyku (null) to dodajemy do db
            if (existingItem == null) {
                db.shoppingCartDao.addItemToCart(item.toEntity(auth.currentUser!!.uid))
            } else { // jesli jest zwiekszamy ilosc
                val newQt = existingItem.quantity + item.quantity
                db.shoppingCartDao.updateQuantity(newQt, item.packageNdc, item.pharmacyId)
            }
        }
    }

    override suspend fun decreaseQt(item: CartItem) {
        if (item.quantity > 1) {
            db.shoppingCartDao.updateQuantity(item.quantity - 1, item.packageNdc, item.pharmacyId)
        }
    }

    override suspend fun increaseQt(item: CartItem) {
        db.shoppingCartDao.updateQuantity(item.quantity + 1, item.packageNdc, item.pharmacyId)
    }

    override suspend fun removeItem(ndc: String, pharmacyId: Int) {
        db.shoppingCartDao.deleteItemFromCart(ndc, pharmacyId)
    }

    override fun observeTotalCartPrice(): Flow<Double> {
        return if (auth.currentUser?.uid == null) {
            flowOf()
        } else {
            db.shoppingCartDao.observeTotalCartPrice(auth.currentUser!!.uid)
        }
    }

    override suspend fun checkIfCanBuy(info: BuyingInfo): Result<CanBuyResponse>  = runCatching {
            val resp = drugApi.checkIfUserCanBuyProduct(info)
            if (!resp.isSuccessful)
                throw retrofit2.HttpException(resp)
            resp.body() ?: throw IllegalStateException("empty body")
        }


    override suspend fun clear() {
        db.shoppingCartDao.deleteAll()
    }

    override suspend fun clearCartForOneUser() {
        db.shoppingCartDao.deleteCartItemsForOneUser(uid)
    }

    override fun observeCart(): Flow<List<CartItem>> {
        return if (auth.currentUser?.uid == null) {
            flowOf(emptyList())
        } else {
            db.shoppingCartDao.observeAll(auth.currentUser!!.uid).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun observeCartSize(): Flow<Int> {
        return if (auth.currentUser?.uid == null) {
            flowOf()
        } else {
            db.shoppingCartDao.getCartSize(auth.currentUser!!.uid)
        }

    }


}
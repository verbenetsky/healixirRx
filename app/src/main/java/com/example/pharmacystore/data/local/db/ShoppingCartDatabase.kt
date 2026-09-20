package com.example.pharmacystore.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pharmacystore.data.local.dao.ShoppingCartDao
import com.example.pharmacystore.data.local.entities.ShoppingCartEntity

@Database(
    entities = [ShoppingCartEntity::class],
    version = 2
)
abstract class ShoppingCartDatabase : RoomDatabase() {
    abstract val shoppingCartDao: ShoppingCartDao

    companion object {
        @Volatile
        private var INSTANCE: ShoppingCartDatabase? = null

        fun getInstance(ctx: Context): ShoppingCartDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    ShoppingCartDatabase::class.java,
                    "shopping_cart.db"
                )
                    .enableMultiInstanceInvalidation()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

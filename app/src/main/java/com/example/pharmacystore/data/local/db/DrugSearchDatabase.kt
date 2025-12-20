package com.example.pharmacystore.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pharmacystore.data.local.dao.DrugSearchDao
import com.example.pharmacystore.data.local.dao.KeysDaoDrug
import com.example.pharmacystore.data.local.dao.ShoppingCartDao
import com.example.pharmacystore.data.local.entities.ActiveIngredientEntity
import com.example.pharmacystore.data.local.entities.DrugEntity
import com.example.pharmacystore.data.local.entities.PackagingEntity
import com.example.pharmacystore.data.local.entities.RemoteKeysDrugEntity
import com.example.pharmacystore.data.local.entities.ShoppingCartEntity
import com.example.pharmacystore.domain.model.ActiveIngredient

@Database(
    entities = [DrugEntity::class, RemoteKeysDrugEntity::class, ActiveIngredientEntity::class, PackagingEntity::class],
    version = 1
)
abstract class DrugSearchDatabase : RoomDatabase() {
    abstract val drugSearchKeysDao: KeysDaoDrug
    abstract val drugSearchDao: DrugSearchDao

    companion object {
        @Volatile
        private var INSTANCE: DrugSearchDatabase? = null

        fun getInstance(ctx: Context): DrugSearchDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    DrugSearchDatabase::class.java,
                    "drug_search.db"
                )
                    .enableMultiInstanceInvalidation()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
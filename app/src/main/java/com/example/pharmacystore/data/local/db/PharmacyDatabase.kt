package com.example.pharmacystore.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pharmacystore.data.local.dao.KeysDaoPharmacy
import com.example.pharmacystore.data.local.dao.PharmacyDao
import com.example.pharmacystore.data.local.entities.PharmacyEntity
import com.example.pharmacystore.data.local.entities.RemoteKeysPharmacyEntity

@Database(
    entities = [PharmacyEntity::class, RemoteKeysPharmacyEntity::class],
    version = 2
)
abstract class PharmacyDatabase : RoomDatabase() {
    abstract val pharmacyDao: PharmacyDao
    abstract val keysDaoPharmacy: KeysDaoPharmacy

    companion object {
        @Volatile
        private var INSTANCE: PharmacyDatabase? = null

        fun getInstance(ctx: Context): PharmacyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    PharmacyDatabase::class.java,
                    "pharmacy.db"
                )
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
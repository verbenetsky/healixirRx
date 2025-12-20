package com.example.pharmacystore.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pharmacystore.data.local.entities.CitiesVillages
import com.example.pharmacystore.data.local.dao.CitiesVillagesDAO
import com.example.pharmacystore.data.local.entities.Streets
import com.example.pharmacystore.data.local.dao.StreetsDAO

@Database(
    entities = [CitiesVillages::class, Streets::class],
    version = 1,
)
abstract class LocationDatabase : RoomDatabase() {
    abstract val streetsDao: StreetsDAO
    abstract val citiesVillagesDao: CitiesVillagesDAO

    companion object {
        @Volatile
        private var INSTANCE: LocationDatabase? = null

        fun getInstance(ctx: Context): LocationDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    LocationDatabase::class.java,
                    "polish_cities_villages_and_streets.db"
                )
                    .createFromAsset("polish_cities_villages_and_streets.db")

                    .enableMultiInstanceInvalidation()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
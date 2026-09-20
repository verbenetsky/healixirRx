package com.example.pharmacystore.data.local.db

import android.content.Context
import androidx.room.Room
import com.example.pharmacystore.data.local.dao.CitiesVillagesDAO
import com.example.pharmacystore.data.local.dao.DrugSearchDao
import com.example.pharmacystore.data.local.dao.KeysDaoDrug
import com.example.pharmacystore.data.local.dao.KeysDaoPharmacy
import com.example.pharmacystore.data.local.dao.PharmacyDao
import com.example.pharmacystore.data.local.dao.ShoppingCartDao
import com.example.pharmacystore.data.local.dao.StreetsDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    // --- LOCATION DATABASE ---

    @Provides
    @Singleton
    fun provideLocationDatabase(@ApplicationContext ctx: Context): LocationDatabase =
        Room.databaseBuilder(
            ctx,
            LocationDatabase::class.java,
            "polish_cities_villages_and_streets.db"
        )
            .createFromAsset("polish_cities_villages_and_streets.db")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun provideStreetsDao(db: LocationDatabase): StreetsDAO = db.streetsDao

    @Provides
    @Singleton
    fun provideCitiesVillagesDao(db: LocationDatabase): CitiesVillagesDAO = db.citiesVillagesDao


    // --- PHARMACY DATABASE ---

    @Provides
    @Singleton
    fun providePharmacyDatabase(@ApplicationContext ctx: Context): PharmacyDatabase =
        Room.databaseBuilder(
            ctx,
            PharmacyDatabase::class.java,
            "pharmacy.db"
        )
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun providePharmacyDao(db: PharmacyDatabase): PharmacyDao = db.pharmacyDao

    @Provides
    @Singleton
    fun providePharmacyKeysDao(db: PharmacyDatabase): KeysDaoPharmacy = db.keysDaoPharmacy

    // --- SHOPPING CART DATABASE ---

    @Provides
    @Singleton
    fun provideShoppingCartDatabase(@ApplicationContext ctx: Context): ShoppingCartDatabase =
        Room.databaseBuilder(
            ctx,
            ShoppingCartDatabase::class.java,
            "shopping_cart.db"
        )
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun provideShoppingCartDao(db: ShoppingCartDatabase): ShoppingCartDao = db.shoppingCartDao

    // --- DRUG SEARCH DATABASE ---

    @Provides
    @Singleton
    fun provideDrugSearchDatabase(@ApplicationContext ctx: Context): DrugSearchDatabase =
        Room.databaseBuilder(
            ctx,
            DrugSearchDatabase::class.java,
            "drug_search.db"
        )
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun provideDrugSearchDao(db: DrugSearchDatabase): DrugSearchDao = db.drugSearchDao

    @Provides
    @Singleton
    fun provideDrugSearchKeysDao(db: DrugSearchDatabase): KeysDaoDrug = db.drugSearchKeysDao
}

package com.example.pharmacystore.di

import com.example.pharmacystore.data.repository.DrugRepositoryImpl
import com.example.pharmacystore.data.repository.ShoppingCartRepositoryImpl
import com.example.pharmacystore.repo.DrugRepository
import com.example.pharmacystore.repo.ShoppingCartRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DrugRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDrugRepo(impl: DrugRepositoryImpl): DrugRepository
}
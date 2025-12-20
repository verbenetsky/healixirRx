package com.example.pharmacystore.di

import com.example.pharmacystore.data.repository.AuthRepositoryImpl
import com.example.pharmacystore.data.repository.UserRepositoryImpl
import com.example.pharmacystore.repo.AuthRepository
import com.example.pharmacystore.repo.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepo(impl: AuthRepositoryImpl): AuthRepository
}
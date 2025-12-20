package com.example.pharmacystore.di

import com.example.pharmacystore.data.repository.UserRepositoryImpl
import com.example.pharmacystore.repo.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // @Binds musi być abstrakcyjną metodą w abstract
    // module i przyjmuje jako parametr już istniejący typ Y, a zwraca X.

    // binds zawsze sie stosuje kiedy jest interfejs -> implementacja
    // „Gdy ktoś poprosi o typ X (interfejs), podaj mu instancję typu Y (implementacja).”
    @Binds
    @Singleton
    abstract fun bindUserRepo(impl: UserRepositoryImpl): UserRepository
}
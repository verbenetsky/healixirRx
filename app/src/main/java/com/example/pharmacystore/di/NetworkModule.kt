package com.example.pharmacystore.di

import com.example.pharmacystore.BuildConfig
import com.example.pharmacystore.remoteApi.DrugApi
import com.example.pharmacystore.remoteApi.OpenFdaApi
import com.example.pharmacystore.remoteApi.PharmacyApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


// wykorzystywane kiedy mamy kilka obiektow tego samego typu i zeby DI wiedzial jakiego stosowac
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KtorRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DrugRetrofit
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val okHttp = provideOkHttp()
    private const val BASE_URL = "https://api.fda.gov/"


    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides @Singleton @KtorRetrofit
    fun provideKtorRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://83.168.69.87:8081/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton @DrugRetrofit
    fun provideDrugRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun providePharmacyApi(@KtorRetrofit retrofit: Retrofit): PharmacyApi =
        retrofit.create(PharmacyApi::class.java)

    @Provides
    @Singleton
    fun provideOpenFdaApi(@DrugRetrofit retrofit: Retrofit): OpenFdaApi =
        retrofit.create(OpenFdaApi::class.java)

    @Provides
    @Singleton
    fun provideDrugApi(@KtorRetrofit retrofit: Retrofit): DrugApi =
        retrofit.create(DrugApi::class.java)

}

fun provideOkHttp(): OkHttpClient {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // BASIC/HEADERS/BODY
    }


    return OkHttpClient.Builder()
        .apply { if (BuildConfig.DEBUG) addInterceptor(logging) } // nie loguj w release
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
}



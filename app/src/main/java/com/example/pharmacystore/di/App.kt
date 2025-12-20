package com.example.pharmacystore.di

import android.app.Application
import com.example.pharmacystore.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // gwarantuje inicjalizacje Firebase
        FirebaseApp.initializeApp(this)
        Places.initialize(this, BuildConfig.MAPS_API_KEY)
    }
}
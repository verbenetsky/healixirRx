package com.example.pharmacystore.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore


private const val SETTINGS_NAME = "cooldown_pref"
val Context.dataStore by preferencesDataStore(name = SETTINGS_NAME)
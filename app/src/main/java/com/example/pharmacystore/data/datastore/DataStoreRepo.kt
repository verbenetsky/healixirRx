package com.example.pharmacystore.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepo @Inject constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveCooldownAndPhoneNumber(phoneNumber: String, time: Long) { // key, value
        dataStore.edit {
            it[longPreferencesKey(phoneNumber)] = time
        }
    }

    // zwraca czas kiedy zostal coolDown uruchomiony, w viewModelu trzeba policzyc ile jescze zostalo czasu
    suspend fun getCooldownStartForPhoneNumber(phoneNumber: String): Long? {
        return dataStore.data.map {
            it[longPreferencesKey(phoneNumber)]
        }.first()
    }
}


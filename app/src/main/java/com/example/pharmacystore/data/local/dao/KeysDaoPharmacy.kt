package com.example.pharmacystore.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pharmacystore.data.local.entities.RemoteKeysPharmacyEntity


@Dao
interface KeysDaoPharmacy {

    @Upsert
    suspend fun upsertAll(listOfKeys: List<RemoteKeysPharmacyEntity>)

    @Query("DELETE FROM remote_key_pharmacy")
    suspend fun deleteAllKeys()

    @Query("SELECT * FROM remote_key_pharmacy where id =:id")
    suspend fun getKey(id: Int): RemoteKeysPharmacyEntity?
}
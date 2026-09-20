package com.example.pharmacystore.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pharmacystore.data.local.entities.RemoteKeysDrugEntity


@Dao
interface KeysDaoDrug {

    @Upsert
    suspend fun upsertAll(listOfKeys: List<RemoteKeysDrugEntity>)

    @Query("DELETE FROM remote_key_drug")
    suspend fun deleteAllKeys()

    @Query("SELECT * FROM remote_key_drug where id =:id")
    suspend fun getKey(id: String): RemoteKeysDrugEntity?
}
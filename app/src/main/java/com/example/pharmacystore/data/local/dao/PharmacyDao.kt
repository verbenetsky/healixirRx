package com.example.pharmacystore.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pharmacystore.data.local.entities.PharmacyEntity

@Dao
interface PharmacyDao {

    @Upsert
    suspend fun upsertAll(pharmacies: List<PharmacyEntity>)

    @Query("SELECT * FROM pharmacyentity ORDER BY remoteOrder ASC")
    fun pagingSource(): PagingSource<Int, PharmacyEntity>


    @Query("DELETE FROM pharmacyentity")
    suspend fun clearAll()
}
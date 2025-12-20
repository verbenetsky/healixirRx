package com.example.pharmacystore.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pharmacystore.data.local.entities.ActiveIngredientEntity
import com.example.pharmacystore.data.local.entities.DrugEntity
import com.example.pharmacystore.data.local.entities.DrugWithDetails
import com.example.pharmacystore.data.local.entities.PackagingEntity

@Dao
interface DrugSearchDao {

    @Upsert
    suspend fun upsertAll(
        drugs: List<DrugEntity>,
        activeIngredients: List<ActiveIngredientEntity>,
        packaging: List<PackagingEntity>
    )

    @Query("SELECT * FROM drug")
    fun pagingSource(): PagingSource<Int, DrugWithDetails>

    @Query("DELETE FROM drug")
    suspend fun clearAllDrugs()

    @Query("DELETE FROM drug_packaging")
    suspend fun clearAllPackaging()

    @Query("DELETE FROM drug_active_ingredient")
    suspend fun clearAllActiveIngredients()

}
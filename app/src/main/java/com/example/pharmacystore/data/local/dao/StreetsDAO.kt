package com.example.pharmacystore.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.pharmacystore.domain.model.StreetsModel


@Dao
interface StreetsDAO {

    @Query("SELECT NAZWA FROM ULIC_Adresowy_2025_07_08 WHERE SYM =:sym " +
            "AND nazwa LIKE '%' || :name || '%' COLLATE NOCASE ORDER BY NAZWA LIMIT 30 ")
    suspend fun getStreetBySymAndName(name: String, sym: String): List<StreetsModel>
}
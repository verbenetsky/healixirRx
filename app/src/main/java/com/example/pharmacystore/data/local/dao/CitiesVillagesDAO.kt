package com.example.pharmacystore.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.pharmacystore.domain.model.CitiesVillagesModel
@Dao
interface CitiesVillagesDAO {

    @Query("SELECT SYM, NAZWA, WOJ, POW, RM, POW_NAZWA FROM lista_wszystkich_miast_wsi_Polska_2025_01_01 WHERE NAZWA LIKE :name || '%' COLLATE NOCASE " +
            "ORDER BY NAZWA LIMIT 30")
    suspend fun getCityOrVillageByName(name: String): List<CitiesVillagesModel>

    @Query("SELECT EXISTS (SELECT 1 from ULIC_Adresowy_2025_07_08 where SYM = :sym LIMIT 1) ")
    suspend fun hasStreets(sym: String): Boolean

    @Query("SELECT COUNT(*) FROM lista_wszystkich_miast_wsi_Polska_2025_01_01")
    suspend fun countAll(): Long

    @Query("SELECT NAZWA FROM lista_wszystkich_miast_wsi_Polska_2025_01_01 LIMIT 5")
    suspend fun firstNames(): List<String>
}


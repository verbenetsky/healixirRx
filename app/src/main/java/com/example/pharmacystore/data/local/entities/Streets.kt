package com.example.pharmacystore.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ULIC_Adresowy_2025_07_08",
    indices = [
        Index(value = ["SYM"]) ,
        // przy filtracji i JOIN-ach bez indeksu taki SELECT bedzie musial przeszukac cala tabele,
        // co przy duze liczbie wierszy spowolni apke
        // cos jak indeksy w firestore
    ]
)
data class Streets(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "WOJ")    val woj: Int?,
    @ColumnInfo(name = "POW")    val pow: Int?,
    @ColumnInfo(name = "GMI")    val gmi: Int?,
    @ColumnInfo(name = "RODZ_GMI") val rodzGmi: Int?,
    @ColumnInfo(name = "SYM")    val sym: Int?,
    @ColumnInfo(name = "SYM_UL") val symUl: Int?,

    @ColumnInfo(name = "NAZWA") val name: String?,
)


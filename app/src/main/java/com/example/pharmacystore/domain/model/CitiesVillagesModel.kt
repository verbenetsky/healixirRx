package com.example.pharmacystore.domain.model

import androidx.room.ColumnInfo

data class CitiesVillagesModel(
    @ColumnInfo(name = "NAZWA") val name: String = "",
    @ColumnInfo(name = "WOJ") val woj: String = "",
    @ColumnInfo(name = "POW") val pow: String = "",
    @ColumnInfo(name = "RM") val rm: String = "",
    @ColumnInfo(name = "SYM") val sym: String = "",
    @ColumnInfo(name = "POW_NAZWA") val powName: String = "",
)
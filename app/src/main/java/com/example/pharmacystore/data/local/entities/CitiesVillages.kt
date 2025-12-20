package com.example.pharmacystore.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "lista_wszystkich_miast_wsi_Polska_2025_01_01",
    indices = [
    Index(value = ["WOJ", "POW"]),
    Index(value = ["NAZWA"])
])
//data class CitiesVillages(
//    @ColumnInfo(name = "WOJ") val woj: String, // numer wojewodztwa
//
//    @ColumnInfo(name = "POW") val pow: String, // kod powiatu
//
//    @ColumnInfo(name = "NAZWA") val name: String, // nazwa miasta lub wsi
//
//    @PrimaryKey // primary key obowiazkowy w kazdej bazie danych nawet jesli tylko ja odczytujemy i nic nie dodajemy
//    @ColumnInfo(name = "SYM") val sym: String, // 7 cyfrowy kod miejscowosci
//
//    @ColumnInfo(name = "RM") val rm: String // 01 - wieś
//                   // 95 dzielnica m. st. Warszawy
//                   // 96 miasto
//                   // 98 delegatura
//                   // 99 część miasta
//)

data class CitiesVillages(
    @PrimaryKey
    @ColumnInfo(name = "SYM")     val sym: Int,     // INTEGER, PK, NOT NULL
    @ColumnInfo(name = "NAZWA")   val nazwa: String?,   // TEXT, NULLABLE
    @ColumnInfo(name = "WOJ")     val woj: Int?,        // INTEGER, NULLABLE
    @ColumnInfo(name = "POW")     val pow: Int?,
    @ColumnInfo(name = "RM")      val rm: Int?,
    @ColumnInfo(name = "POW_NAZWA")      val powName: String?,
    // opcjonalnie dodaj pozostałe kolumny,
    // jeżeli chcesz z nich korzystać:
    @ColumnInfo(name = "GMI")     val gmi: Int?,
    @ColumnInfo(name = "RODZ_GMI")val rodzGmi: Int?,
    @ColumnInfo(name = "SYMPOD")  val sympod: Int?,
)


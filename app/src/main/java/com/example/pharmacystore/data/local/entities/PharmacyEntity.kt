package com.example.pharmacystore.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PharmacyEntity(
    @PrimaryKey
    val identyfikator_apteki: Int,
    val nazwa_apteki: String?,
    val rodzaj_apteki: String,
    val powiat: String,
    val gmina: String?,
    val typ_ulicy: String?,
    val ulica_znormalizowana: String,
    val miejscowosc: String?,
    val distance: Double,
    val lat: Double?,
    val lon: Double?,
    val isOpenNow: Boolean,
    val remoteOrder: Long
)
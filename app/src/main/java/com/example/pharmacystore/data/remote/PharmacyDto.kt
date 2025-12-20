package com.example.pharmacystore.data.remote

data class PharmacyDto(
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
    val isOpenNow: Boolean
)
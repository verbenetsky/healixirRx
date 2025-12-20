package com.example.pharmacystore.data.mappers

import com.example.pharmacystore.data.local.entities.PharmacyEntity
import com.example.pharmacystore.data.remote.PharmacyDto
import com.example.pharmacystore.ui.pharmacies.PharmacyLight

fun PharmacyDto.toPharmacyEntity(remoteOrder: Long): PharmacyEntity {
    return PharmacyEntity(
        identyfikator_apteki = identyfikator_apteki,
        nazwa_apteki = nazwa_apteki,
        rodzaj_apteki = rodzaj_apteki,
        powiat = powiat,
        gmina = gmina,
        typ_ulicy = typ_ulicy,
        ulica_znormalizowana = ulica_znormalizowana,
        miejscowosc = miejscowosc,
        distance = distance,
        lat = lat,
        lon = lon,
        isOpenNow = isOpenNow,
        remoteOrder = remoteOrder
    )
}

fun PharmacyEntity.toPharmacy() : PharmacyLight {
    return PharmacyLight(
        identyfikator_apteki = identyfikator_apteki,
        nazwa_apteki = nazwa_apteki,
        rodzaj_apteki = rodzaj_apteki,
        powiat = powiat,
        gmina = gmina,
        typ_ulicy = typ_ulicy,
        ulica_znormalizowana = ulica_znormalizowana,
        miejscowosc = miejscowosc,
        distance = distance,
        lat = lat,
        lon = lon,
        isOpenNow = isOpenNow
    )
}
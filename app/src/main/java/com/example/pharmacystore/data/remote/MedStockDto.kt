package com.example.pharmacystore.data.remote


data class PharmacyShortDto(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String,
    val distanceKms: Double
)

data class MedStockDto(
    val pharmacy: PharmacyShortDto,
    val drugPrice: Double,
    val quantity: Int,
    val packageNdc: String
)

data class PharmacyShort(
    val id: Int,
    val name: String,
    val address: String?,
    val city: String,
    val distanceKms: Double
)

data class MedStock(
    val pharmacy: PharmacyShort,
    val drugPrice: Double,
    val quantity: Int,
    val packageNdc: String
)

fun PharmacyShort.toPharmacyShortDto(): PharmacyShortDto {
    return PharmacyShortDto(
        id = id,
        name = name,
        address = address,
        city = city,
        distanceKms = distanceKms
    )
}

fun PharmacyShortDto.toPharmacyShort(): PharmacyShort {
    return PharmacyShort(
        id = id,
        name = name,
        address = address,
        city = city,
        distanceKms = distanceKms
    )
}

fun MedStock.toMedStockDto() : MedStockDto {
    return MedStockDto(
        pharmacy = pharmacy.toPharmacyShortDto(),
        drugPrice = drugPrice,
        quantity = quantity,
        packageNdc = packageNdc
    )
}

fun MedStockDto.toMedStock() : MedStock {
    return MedStock(
        pharmacy = pharmacy.toPharmacyShort(),
        drugPrice = drugPrice,
        quantity = quantity,
        packageNdc = packageNdc
    )
}

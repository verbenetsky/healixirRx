package com.example.pharmacystore.domain.model

import com.example.pharmacystore.data.local.entities.ShoppingCartEntity
import com.example.pharmacystore.data.remote.MedStock

data class CartItem(
    val packageNdc: String,
    val drugNDC: String,
    val name: String,
    val labelerName: String,
    val brandName: String?,
    val quantity: Int = 1,
    val drugPackageDesc: String,

    val pharmacyId: Int,
    val pharmacyName: String,
    val address: String?,
    val city: String,
    val distanceKms: Double
)


fun CartItem.toEntity(userUID: String): ShoppingCartEntity {
    return ShoppingCartEntity(
        name = name,
        labelerName = labelerName,
        brandName = brandName,
        drugNDC = drugNDC,
        packageNDC = packageNdc,
        quantity = quantity,
        drugPackageDesc = drugPackageDesc,
        pharmacyId = pharmacyId,
        pharmacyName = pharmacyName,
        address = address,
        city = city,
        distanceKms = distanceKms,
        userUID = userUID
    )
}

fun ShoppingCartEntity.toDomain() = CartItem(
    packageNdc = packageNDC,
    drugNDC = drugNDC,
    name = name,
    labelerName = labelerName,
    brandName = brandName,
    quantity = quantity,
    drugPackageDesc = drugPackageDesc,
    pharmacyId = pharmacyId,
    pharmacyName = pharmacyName,
    address = address,
    city = city,
    distanceKms = distanceKms
)


package com.example.pharmacystore.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    "shopping_cart",
    primaryKeys = ["packageNDC", "pharmacyId"]
)
data class ShoppingCartEntity(

    val userUID: String,

    val packageNDC: String,
    val name: String,
    val labelerName: String,
    val brandName: String?,
    val drugNDC: String,
    val quantity: Int,
    val price: Double,
    val drugPackageDesc: String,

    val pharmacyId: Int,
    val pharmacyName: String,
    val address: String?,
    val city: String,
    val distanceKms: Double
)



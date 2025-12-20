package com.example.pharmacystore.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("remote_key_drug")
data class RemoteKeysPharmacyEntity(
    @PrimaryKey
    val id: Int,
    val nextKey: Int?
)
package com.example.pharmacystore.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("remote_key_drug")
data class RemoteKeysDrugEntity(
    @PrimaryKey
    val id: String,
    val nextKey: Int?
)
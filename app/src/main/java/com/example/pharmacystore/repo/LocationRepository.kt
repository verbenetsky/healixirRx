package com.example.pharmacystore.repo

import com.example.pharmacystore.data.repository.GeoPoint
import com.example.pharmacystore.data.repository.LocationRepositoryImpl
import kotlinx.coroutines.flow.StateFlow


interface LocationRepository {

    val location: StateFlow<GeoPoint?>
    suspend fun geocode(address: String, country: String = "PL"): Result<GeoPoint>
}
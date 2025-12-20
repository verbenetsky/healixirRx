package com.example.pharmacystore.data.repository

import com.example.pharmacystore.repo.LocationRepository
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient,
) : LocationRepository {

    private val _location = MutableStateFlow<GeoPoint?>(null)
    override val location: StateFlow<GeoPoint?> = _location.asStateFlow()

    override suspend fun geocode(address: String, country: String): Result<GeoPoint> {
        val token = AutocompleteSessionToken.newInstance()
        return runCatching {
            val preds = placesClient.findAutocompletePredictions(
                FindAutocompletePredictionsRequest.builder()
                    .setQuery(address)
                    .setCountries(listOf(country))
                    .setSessionToken(token)
                    .build()
            ).await().autocompletePredictions

            val first = preds.firstOrNull() ?: error("NO_PREDICTIONS")

            val fields = listOf(Place.Field.ID, Place.Field.LOCATION)
            val place = placesClient.fetchPlace(
                FetchPlaceRequest.builder(first.placeId, fields)
                    .setSessionToken(token)
                    .build()
            ).await().place
            val lat = place.location?.latitude ?: error("Empty latitude")
            val lon = place.location?.longitude ?: error("Empty latitude")

            _location.value = GeoPoint(lat, lon)

            GeoPoint(lat, lon)

        }.onFailure { e ->
            if (e is CancellationException) throw e
        }
    }
}


data class GeoPoint(val lat: Double, val lon: Double)


package com.example.pharmacystore.data.local.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import coil.network.HttpException
import com.example.pharmacystore.data.local.db.PharmacyDatabase
import com.example.pharmacystore.data.local.entities.PharmacyEntity
import com.example.pharmacystore.data.local.entities.RemoteKeysPharmacyEntity
import com.example.pharmacystore.data.mappers.toPharmacyEntity
import com.example.pharmacystore.remoteApi.CoordinatesDataDto
import com.example.pharmacystore.remoteApi.PharmacyApi
import com.example.pharmacystore.repo.LocationRepository
import com.example.pharmacystore.repo.PharmacyRepository
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class PharmaciesRemoteMediator(
    private val pharmacyApi: PharmacyApi,
    private val db: PharmacyDatabase,
    private val pharmacyRepository: PharmacyRepository,
    private val locationRepository: LocationRepository,
) : RemoteMediator<Int, PharmacyEntity>() {

    // fun to zostanie uruchomiona kiedy jest jakas forma loading w odniesieniu do
    // pagination, moga byc rozne typy loading, za to odpowiada loadType
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PharmacyEntity> // odnosi sie do stanu aktualnej strony na ktorej sie znajdujemy
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(true)
                LoadType.APPEND -> {

                    // jesli nie mamy ostatniego elementu, czyli baza jest pusta i nie ma tam niczego zeby doklejac
                    // takie moze byc dopiero na poczatku kiedy jescze nie zostalo pobrane niczego

                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(true)

                    // jesli nie mamy zadnego klucza przyisanego do ostatniej apteki to cos poszlo nie tak i wychodziym
                    val key = db.keysDaoPharmacy.getKey(id = lastItem.identyfikator_apteki)
                        ?: return MediatorResult.Success(true)

                    key.nextKey ?: return MediatorResult.Success(true)
                }
            }

            val loc = locationRepository.location.value ?: return MediatorResult.Success(true)

            val pharmacies = pharmacyApi.getNearbyPharmacies(
                page = loadKey,
                perPage = state.config.pageSize,
                sortOrder = pharmacyRepository.sortOrder.value,
                sortedBy = pharmacyRepository.sortedBy.value,
                isOpen = pharmacyRepository.isOpen.value,
                coordinatesData = CoordinatesDataDto(
                    lat = loc.lat,
                    lon = loc.lon,
                    radiusMeters = pharmacyRepository.radius.value * 1000
                )
            )

            println("wyslalo sie do serwera")

            // jesli ilosc zwroconych rekordow jest mniejsza od ilosci elementow na stronie oznacza to ze jest to koniec
            // i nic wiecej nie da sie juz pobrac
            val end = pharmacies.size < state.config.pageSize

            println("end is: $end, and pharmacies.size is ${pharmacies.size} ")

            // liczym nastepna strone (key = strona)
            val nextKey = if (end) null else loadKey + 1

            println("next key is: $nextKey")

            db.withTransaction { // wysztko sie musi wykonac
                if (loadType == LoadType.REFRESH) {
                    db.pharmacyDao.clearAll()
                    db.keysDaoPharmacy.deleteAllKeys()
                }

                val pageSize = state.config.pageSize; val baseIndex = (loadKey - 1) * pageSize

                val pharmacyEntities = pharmacies.mapIndexed { index, dto ->
                    dto.toPharmacyEntity(remoteOrder = (baseIndex + index).toLong())
                }

                val keys =
                    pharmacies.map { RemoteKeysPharmacyEntity(it.identyfikator_apteki, nextKey) }

                db.keysDaoPharmacy.upsertAll(keys)

                db.pharmacyDao.upsertAll(pharmacyEntities)
            }

            MediatorResult.Success(endOfPaginationReached = end)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
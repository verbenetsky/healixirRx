package com.example.pharmacystore.data.local.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import com.example.pharmacystore.data.local.db.DrugSearchDatabase
import com.example.pharmacystore.data.local.entities.DrugWithDetails
import com.example.pharmacystore.data.local.entities.RemoteKeysDrugEntity
import com.example.pharmacystore.data.mappers.toActiveIngredientsEntities
import com.example.pharmacystore.data.mappers.toDrugEntity
import com.example.pharmacystore.data.mappers.toPackagingEntities
import com.example.pharmacystore.remoteApi.OpenFdaApi
import com.example.pharmacystore.repo.DrugRepository
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class DrugRemoteMediator(
    private val drugApi: OpenFdaApi,
    private val db: DrugSearchDatabase,
    private val repo: DrugRepository
) :
    RemoteMediator<Int, DrugWithDetails>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DrugWithDetails> // odnosi sie do stanu aktualnej strony na ktorej sie znajdujemy
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(true)

                    val key = db.drugSearchKeysDao.getKey(id = lastItem.drug.drugNDC)
                        ?: return MediatorResult.Success(true)

                    key.nextKey ?: return MediatorResult.Success(true)
                }
            }

            val typeOfInput = classifyInput(repo.searchBarState.value)
            println("type of input is: $typeOfInput")

            val query = repo.searchBarState.value
            val response = try {
                when (typeOfInput) {
                    QueryType.PRODUCT_NDC -> {
                        println("query is: $query")
                        drugApi.getDrugByProductNDC(
                            search = """product_ndc:"$query"""",
                            skip = (loadKey - 1) * state.config.pageSize,
                            limit = state.config.pageSize
                        )
                    }

                    QueryType.PACKAGE_NDC -> {
                        drugApi.getDrugByPackageNDC(
                            search = """packaging.package_ndc:"$query"""",
                            skip = (loadKey - 1) * state.config.pageSize,
                            limit = state.config.pageSize
                        )
                    }

                    QueryType.UPC -> {
                        drugApi.getDrugByUPC(
                            search = """openfda.upc:"$query"""",
                            skip = (loadKey - 1) * state.config.pageSize,
                            limit = state.config.pageSize
                        )
                    }

                    QueryType.NAME -> {
                        drugApi.getDrugByName(
                            search = query,
                            // albo np. """generic_name:"$query*"""" zeby byl prefix-match
                            skip = (loadKey - 1) * state.config.pageSize,
                            limit = state.config.pageSize
                        )
                    }
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // brak wyników w openFDA
                    if (loadType == LoadType.REFRESH) {
                        db.withTransaction {
                            db.drugSearchDao.clearAllActiveIngredients()
                            db.drugSearchDao.clearAllDrugs()
                            db.drugSearchDao.clearAllPackaging()
                            db.drugSearchKeysDao.deleteAllKeys()
                        }
                    }
                    return MediatorResult.Success(endOfPaginationReached = true)
                } else {
                    return MediatorResult.Error(e)
                }
            } catch (e: IOException) {
                return MediatorResult.Error(e)
            }

            println("response is: $response")

            val end = response.results.size < state.config.pageSize

            val nextKey = if (end) null else loadKey + 1

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.drugSearchDao.clearAllActiveIngredients()
                    db.drugSearchDao.clearAllDrugs()
                    db.drugSearchDao.clearAllPackaging()
                    db.drugSearchKeysDao.deleteAllKeys()
                }

                val drugEntity = response.results.map { it.toDrugEntity() }
                val activeIngredientEntity =
                    response.results.flatMap { it.toActiveIngredientsEntities() }
                val packagingEntity = response.results.flatMap { it.toPackagingEntities() }

                val keys = response.results.map { RemoteKeysDrugEntity(it.drugNDC, nextKey) }

                db.drugSearchKeysDao.upsertAll(keys)

                db.drugSearchDao.upsertAll(drugEntity, activeIngredientEntity, packagingEntity)
            }

            MediatorResult.Success(endOfPaginationReached = end)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

enum class QueryType { PRODUCT_NDC, PACKAGE_NDC, UPC, NAME }

/**
 * Rozpoznaje, co wpisał użytkownik:
 *  • product-level NDC  (4-4, 5-3, 5-4)            → 58151-503
 *  • package-level NDC  (4-4-2, 5-3-2, 5-4-1)      → 58151-503-91
 *  • UPC / GTIN        (12- lub 14-cyfrowy ciąg)  → 0300746799221
 *  • wszystko inne     → np. „Xanax”
 */
fun classifyInput(raw: String): QueryType {
    val s = raw.trim()

    return when {
        // 4-5 cyfr – 3-4 cyfry – 1-2 cyfry (dwa myślniki)
        s.matches(Regex("""^\d{4,5}-\d{3,4}-\d{1,2}$""")) -> QueryType.PACKAGE_NDC

        // 4-5 cyfr – 3-4 cyfry (jeden myślnik)
        s.matches(Regex("""^\d{4,5}-\d{3,4}$""")) -> QueryType.PRODUCT_NDC

        // czysty 11-cyfrowy NDC-11 = package-level
        s.matches(Regex("""^\d{11}$""")) -> QueryType.PACKAGE_NDC

        // czysty 10-cyfrowy NDC-10 = product-level
        s.matches(Regex("""^\d{10}$""")) -> QueryType.PRODUCT_NDC

        // 12- lub 14-cyfrowy UPC / GTIN
        s.matches(Regex("""^\d{12,14}$""")) -> QueryType.UPC

        else -> QueryType.NAME
    }
}



package com.example.pharmacystore.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.pharmacystore.data.local.db.DrugSearchDatabase
import com.example.pharmacystore.data.local.entities.DrugWithDetails
import com.example.pharmacystore.data.local.mediator.DrugRemoteMediator
import com.example.pharmacystore.data.remote.MedStockDrugDto
import com.example.pharmacystore.data.remote.MedStockDto
import com.example.pharmacystore.remoteApi.OpenFdaApi
import com.example.pharmacystore.remoteApi.DrugApi
import com.example.pharmacystore.repo.DrugRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrugRepositoryImpl @Inject constructor(
    private val fdaApi: OpenFdaApi,
    private val drugApi: DrugApi,
    private val openFdaApi: OpenFdaApi,
    private val db: DrugSearchDatabase,
) : DrugRepository {

    private val _searchBarState = MutableStateFlow("")
    override val searchBarState = _searchBarState.asStateFlow()

    override fun updateSearchBarState(value: String) {
        _searchBarState.value = value
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun paged(searchBarState: String): Flow<PagingData<DrugWithDetails>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = DrugRemoteMediator(
                drugApi = fdaApi,
                db = db,
                repo = this
            ),
            pagingSourceFactory = { db.drugSearchDao.pagingSource() }
        ).flow
    }

    override suspend fun checkMedStock(
        packageNdc: String,
        usersLat: Double,
        usersLon: Double
    ): Result<List<MedStockDto>> {
        if (packageNdc.isEmpty()) return Result.success(emptyList())

        return runCatching {
            drugApi.getMedStock(packageNdc, usersLat, usersLon)
        }
    }

    override suspend fun getPharmacyStock(pharmacyId: Int): Result<List<MedStockDrugDto>> {
        return runCatching {
            drugApi.getPharmacyStock(pharmacyId)
        }
    }

    override suspend fun getUpcCodes(drugNdc: String): Result<List<String>> {
        return runCatching {
            val response = openFdaApi.getUpcCodeForDrugNdc(drugNdc)
            val allUpc: List<String> = response.results
                .flatMap { res -> res.openFda?.upc.orEmpty() }
                .distinct()
            allUpc
        }
    }

    // brand_name - nazwa handlowa leku tak jak w PL ibuprom
    // https://api.fda.gov/drug/ndc.json?search=brand_name%3A%ibuprofen%
//    override suspend fun getDragByName(name: String): Result<List<Drug>?> =
//        runCatching {
//            api.getDrugByName(
//                search = "brand_name:${name}",
//                limit = 99
//            )
//        }
//
//    // https://api.fda.gov/drug/ndc.json?search=openfda.upc.exact%3A0300698140201&limit=99
//    override suspend fun getDragByUPC(upc: String): Result<List<Drug>?> =
//        runCatching {
//            api.getDrugByUPC(
//                search = "openfda.upc.exact:${upc}",
//                limit = 99
//            )
//
//        }
//
//    // wyszukiwanie po product NDC zawsze (zawsze na 100proc) zawiera w sobie package NDC
//    //"product_ndc": "58151-503",
//    // "package_ndc": "58151-503-91"
//    // etc
//
//    override suspend fun getDragByPackageNDC(ndc: String): Result<List<Drug>?> =
//        runCatching {
//            api.getDrugByUPC(
//                search = "packaging.package_ndc:$ndc",
//                limit = 99
//            )
//
//        }
//
//    // wywolac jesli getDrugByProductNDC sie nie udalo
//    // https://api.fda.gov/drug/ndc.json?search=product_ndc%3A%220009-0094%22&limit=1 example
//    override suspend fun getDragByProductNDC(ndc: String): Result<List<Drug>?> =
//        runCatching {
//            api.getDrugByUPC(
//                search = "product_ndc:$ndc",
//                limit = 99
//            )
//
//        }
}


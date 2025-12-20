package com.example.pharmacystore.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Drug(
    @Json(name = "generic_name")
    val genericName: String,

    @Json(name = "labeler_name")
    val labelerName: String,

    @Json(name = "brand_name")
    val brandName: String? = null,

    @Json(name = "product_ndc")
    val drugNDC: String,

    @Json(name = "active_ingredients")
    val activeIngredients: List<ActiveIngredient> = emptyList(),

    val packaging: List<Packaging> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Packaging(
    @Json(name = "package_ndc")
    val packageNdc: String,
    @Json(name = "description")
    val desc: String
)

@JsonClass(generateAdapter = true)
data class ActiveIngredient(
    val name: String,
    val strength: String? = null
)


val Drug.displayName: String
    get() {
        val g = genericName.trim()
        val b = (brandName ?: "").trim()
        return when {
            b.isNotEmpty() && b != g -> "$b ($g)"
            g.isNotEmpty()          -> g
            else                    -> "– nieznany lek –"
        }
    }



@JsonClass(generateAdapter = true)
data class OpenFdaDto(
    val upc: List<String>? = emptyList()
)
@JsonClass(generateAdapter = true)
data class ResultResponseDto(
    @Json(name = "openfda")
    val openFda: OpenFdaDto? = null
)
@JsonClass(generateAdapter = true)
data class UpcResponseDto(
    val results: List<ResultResponseDto> = emptyList()
)

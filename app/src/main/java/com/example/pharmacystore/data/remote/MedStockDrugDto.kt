package com.example.pharmacystore.data.remote


// odpowiada za pojedynczy rekord leku znajdujacego sie w konkretnej aptece
data class MedStockDrugDto (
    val packageNdc: String,
    val drugNdc: String,
    val genericName: String?,
    val labelerName: String?,
    val brandName: String?,
    val qt: Int,
    val price: Double,
)

data class MedStockDrug (
    val packageNdc: String,
    val drugNdc: String,
    val genericName: String?,
    val labelerName: String?,
    val brandName: String?,
    val qt: Int,
    val price: Double,
)

val MedStockDrug.displayName: String
    get() {
        val g = genericName?.trim() ?: ""
        val b = (brandName ?: "").trim()
        return when {
            b.isNotEmpty() && b != g -> "$b ($g)"
            g.isNotEmpty()          -> g
            else                    -> "– nieznany lek –"
        }
    }


fun MedStockDrugDto.toDrug(): MedStockDrug {
    return MedStockDrug(
        packageNdc = packageNdc,
        drugNdc = drugNdc,
        genericName = genericName,
        labelerName = labelerName,
        brandName = brandName,
        qt = qt,
        price = price
    )
}

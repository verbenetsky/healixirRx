package com.example.pharmacystore.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity("drug")
data class DrugEntity(
    val genericName: String,
    val labelerName: String,
    val brandName: String? = null,
    @PrimaryKey val drugNDC: String
)

@Entity(
    "drug_active_ingredient",
    foreignKeys = [
        ForeignKey(
            entity = DrugEntity::class,
            parentColumns = ["drugNDC"],
            childColumns = ["drugNDC"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [Index("drugNDC")]
)
data class ActiveIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugNDC: String,
    val name: String,
    val strength: String?
)

@Entity("drug_packaging",
    foreignKeys = [
        ForeignKey(
            entity = DrugEntity::class,
            parentColumns = ["drugNDC"],
            childColumns = ["drugNDC"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [Index("drugNDC")])
data class PackagingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugNDC: String,
    val packageNdc: String,
    val description: String
)

data class DrugWithDetails(
    @Embedded val drug: DrugEntity,
    @Relation(
        parentColumn = "drugNDC",
        entityColumn = "drugNDC"
    )
    val activeIngredients: List<ActiveIngredientEntity>,
    @Relation(
        parentColumn = "drugNDC",
        entityColumn = "drugNDC"
    )
    val packaging: List<PackagingEntity>
)

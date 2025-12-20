package com.example.pharmacystore.data.mappers

import com.example.pharmacystore.data.local.entities.ActiveIngredientEntity
import com.example.pharmacystore.data.local.entities.DrugEntity
import com.example.pharmacystore.data.local.entities.DrugWithDetails
import com.example.pharmacystore.data.local.entities.PackagingEntity
import com.example.pharmacystore.domain.model.ActiveIngredient
import com.example.pharmacystore.domain.model.Drug
import com.example.pharmacystore.domain.model.Packaging

fun Drug.toDrugEntity(): DrugEntity {
    return DrugEntity(
        genericName = genericName,
        labelerName = labelerName,
        brandName = brandName,
        drugNDC = drugNDC
    )
}

fun Drug.toActiveIngredientsEntities(): List<ActiveIngredientEntity> {
    return activeIngredients.map { drug ->
        ActiveIngredientEntity(
            drugNDC = drugNDC,
            name = drug.name,
            strength = drug.strength
        )
    }
}

fun Drug.toPackagingEntities(): List<PackagingEntity> {
    return packaging.map { drug ->
        PackagingEntity(
            drugNDC = drugNDC,
            packageNdc = drug.packageNdc,
            description = drug.desc
        )
    }
}

fun DrugWithDetails.toDrug(): Drug =
    Drug(
        genericName = drug.genericName,
        labelerName = drug.labelerName,
        brandName = drug.brandName,
        drugNDC = drug.drugNDC,
        activeIngredients = activeIngredients.map { it.toDomain() },
        packaging = packaging.map { it.toDomain() }
    )

fun ActiveIngredientEntity.toDomain() = ActiveIngredient(
    name = name,
    strength = strength
)

fun PackagingEntity.toDomain() = Packaging(
    packageNdc = packageNdc,
    desc = description
)
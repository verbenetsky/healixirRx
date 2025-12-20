package com.example.pharmacystore.domain.model

data class Validation(
    val email: Boolean = true,
    val password: Boolean = true
)

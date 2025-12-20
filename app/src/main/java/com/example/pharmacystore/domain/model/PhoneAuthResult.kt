package com.example.pharmacystore.domain.model

import com.google.firebase.auth.FirebaseUser

data class PhoneAuthResult(
    val user: FirebaseUser,
    val isNewUser: Boolean
)
package com.example.pharmacystore.repo

import android.app.Activity
import com.example.pharmacystore.domain.model.PhoneAuthResult

interface AuthRepository {

    suspend fun sendConfirmationSms(phoneNumber: String, activity: Activity): Result<String>

    suspend fun verifySmsCode(verificationId: String, code: String): Result<PhoneAuthResult>

    suspend fun signUpUser(email: String, password: String): Result<Unit>

    suspend fun signInUser(email: String, password: String): Result<Unit>

    suspend fun linkEmail() // dodaje do numer telefonu do istniejacego email + haslo
    suspend fun linkPhoneToCurrentUser(smsCode: String, verificationId: String): Result<Unit> // dodaje do istniejacego email + haslo numer telefonu
}
package com.example.pharmacystore.repo

import android.app.Activity
import com.example.pharmacystore.domain.model.PhoneAuthResult

interface AuthRepository {

    // zasada wlaczenia 2fa jest taka:
    // 1. Zrobic re-auth, czyli zalogowac ponownie usera (jesli user jest zalogowany numerem telefonu to trzeba go zalogowac za pomoca emaila i hasla)
    suspend fun reAuth(password: String): Result<Unit>

    suspend fun mfaEnrollment(phoneNumber: String, activity: Activity): Result<String>

    suspend fun completeEnrollment(verificationId: String, code: String): Result<Unit>
    suspend fun checkIfEmailIsVerified(): Result<Boolean>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun sendConfirmationSms(phoneNumber: String, activity: Activity): Result<String>

    suspend fun verifySmsCode(verificationId: String, code: String): Result<PhoneAuthResult>

    suspend fun signUpUser(email: String, password: String): Result<Unit>

    suspend fun signInUser(email: String, password: String): Result<Unit>

    suspend fun linkEmail(email: String, password: String): Result<Unit>
    // dodaje do numer telefonu do istniejacego email + haslo

    suspend fun linkPhoneToCurrentUser(smsCode: String, verificationId: String): Result<Unit>
    // dodaje do istniejacego email + haslo numer telefonu
}
package com.example.pharmacystore.data.repository

import android.app.Activity
import com.example.pharmacystore.domain.model.PhoneAuthResult
import com.example.pharmacystore.repo.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    // Wysyła SMS i zwraca verificationId w momencie onCodeSent
    override suspend fun sendConfirmationSms(
        phoneNumber: String,
        activity: Activity
    ): Result<String> = suspendCancellableCoroutine { cont ->

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                if (cont.isActive) {
                    cont.resume(Result.success(verificationId))
                }
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Opcjonalnie: auto-logowanie bez wpisywania kodu (jeśli chcesz to obsłużyć)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (cont.isActive) {
                    cont.resume(Result.failure(e))
                }
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                // To tylko informacja o końcu auto-retrieval — ręczne wpisanie kodu nadal działa.

            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setActivity(activity)               // ważne: Activity, nie sam Context
            .setTimeout(
                60L,
                TimeUnit.SECONDS
            )   // czas okna auto-retrieval (nie blokuje ręcznego wpisu)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        // Jeśli korutyna zostanie anulowana (np. użytkownik opuści ekran), nic nie robimy.
        cont.invokeOnCancellation {
            // Tu ewentualnie można posprzątać zasoby, ale PhoneAuthProvider nie udostępnia cancel.
        }
    }

    // Weryfikuje podany kod i loguje; zwraca info czy to nowy użytkownik
    override suspend fun verifySmsCode(
        verificationId: String,
        code: String
    ): Result<PhoneAuthResult> = runCatching {

        require(verificationId.isNotBlank()) { "verificationId is blank" }
        require(code.isNotBlank()) { "code is blank" }

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val authResult = auth.signInWithCredential(credential).await()

        val user = authResult.user ?: error("No user returned")
        val isNew = authResult.additionalUserInfo?.isNewUser == true
        PhoneAuthResult(user, isNew)
    }

    override suspend fun signUpUser(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                auth.createUserWithEmailAndPassword(email, password).await()
                Unit
            }
        }

    override suspend fun signInUser(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                auth.signInWithEmailAndPassword(email, password).await()
                Unit
            }
        }

    override suspend fun linkEmail() {
//        val phoneCred = PhoneAuthProvider.getCredential(verificationId, smsCode)
//
//        val user = FirebaseAuth.getInstance().currentUser ?: return
//        user.linkWithCredential(phoneCred)
    }

    override suspend fun linkPhoneToCurrentUser(
        smsCode: String,
        verificationId: String
    ): Result<Unit> = runCatching {
        require(smsCode.isNotBlank()) { "verificationId is blank" }
        require(verificationId.isNotBlank()) { "code is blank" }

        val user = auth.currentUser ?: error("User not logged in")

        val credential = PhoneAuthProvider.getCredential(smsCode, verificationId)

        user.linkWithCredential(credential).await()
        Unit
    }


}

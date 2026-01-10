package com.example.pharmacystore.data.repository

import android.app.Activity
import android.util.Log
import com.example.pharmacystore.domain.model.PhoneAuthResult
import com.example.pharmacystore.repo.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepositoryImpl @Inject constructor(private val auth: FirebaseAuth) : AuthRepository {

    // Zrobic re-auth, czyli zalogowac ponownie usera (jesli user jest zalogowany numerem telefonu to trzeba go zalogowac za pomoca emaila i hasla)
    override suspend fun reAuth(password: String): Result<Unit> = runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: return@runCatching
        val email: String? = user.email

        // jesli email jest null to user jest zalogowany numerem telefonu wiec trzeba poprosic go zeby zalogowal sie za pomoca maila i hasla
        if (email == null) {

        } else {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
        }
    }


    override suspend fun checkIfEmailIsVerified(): Result<Boolean> = runCatching {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return@runCatching false

        user.reload().await()
        user.isEmailVerified
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("User not logged in")
        user.sendEmailVerification().await()
        Unit
    }

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
                // Opcjonalnie: auto-logowanie bez wpisywania kodu
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
            .setActivity(activity)
            .setTimeout(
                60L,
                TimeUnit.SECONDS
            )   // czas okna auto-retrieval (nie blokuje ręcznego wpisu)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        // Jeśli korutyna zostanie anulowana (np. użytkownik opuści ekran), nic nie robimy.
        // cont.invokeOnCancellation {
    }

    override suspend fun mfaEnrollment(
        phoneNumber: String,
        activity: Activity
    ): Result<String> = runCatching {

        val user = FirebaseAuth.getInstance().currentUser ?: error("User not logged in")
        val session = user.multiFactor.session.await()

        suspendCancellableCoroutine { cont ->

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    if (cont.isActive) cont.resume(verificationId)
                }

                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1) Instant verification. In some cases, the phone number can be
                    //    instantly verified without needing to send or enter a verification
                    //    code. You can disable this feature by calling
                    //    PhoneAuthOptions.builder#requireSmsValidation(true) when building
                    //    the options to pass to PhoneAuthProvider#verifyPhoneNumber().
                    // 2) Auto-retrieval. On some devices, Google Play services can
                    //    automatically detect the incoming verification SMS and perform
                    //    verification without user action.
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in response to invalid requests for
                    // verification, like an incorrect phone number.
                    val fae = e as? FirebaseAuthException
                    Log.e("TWOFA", "type=${e.javaClass.name} code=${fae?.errorCode} msg=${e.message}", e)
                    if (cont.isActive) cont.resumeWithException(e)
                }
            }

            val phoneAuthOptions = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setActivity(activity)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setMultiFactorSession(session)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)

            cont.invokeOnCancellation {
                // Brak twardego cancel dla verifyPhoneNumber; ignorujemy callbacki.
                // sprzatanie
            }
        }
    }


    override suspend fun completeEnrollment(verificationId: String, code: String): Result<Unit> =
        runCatching {
            val user = FirebaseAuth.getInstance().currentUser ?: error("User not logged in")
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential)

            user.multiFactor.enroll(multiFactorAssertion, "My personal phone number").await()
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
                // val user = auth.currentUser ?: error("User not logged in")
                // user.sendEmailVerification().await()
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

    override suspend fun linkEmail(email: String, password: String) = runCatching {
        require(email.isNotBlank()) { "verificationId is blank" }
        require(password.isNotBlank()) { "code is blank" }

        val user = auth.currentUser ?: error("User not logged in")
        val credential = EmailAuthProvider.getCredential(email, password)

        user.linkWithCredential(credential)
        println("email linked")
        Unit
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
    }
}

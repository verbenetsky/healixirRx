package com.example.pharmacystore.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.data.datastore.DataStoreRepo
import com.example.pharmacystore.data.local.phoneprefixes.CountryPrefix
import com.example.pharmacystore.repo.AuthRepository
import com.example.pharmacystore.repo.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AuthSmsViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val userRepo: UserRepository,
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    private val _events = MutableSharedFlow<AuthSmsUiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    private val cooldownSec = 60L

    private val _remainingSec = MutableStateFlow(0L)               // ile sekund zostało
    val remainingSec: StateFlow<Long> = _remainingSec.asStateFlow()

    private var countdownJob: Job? = null

    private val _uiState = MutableStateFlow<AuthSmsUiState>(AuthSmsUiState.Idle)
    val uiState: StateFlow<AuthSmsUiState> = _uiState.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")                 // local-part (bez prefiksu)
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    fun updatePhoneNumber(newValue: String) {
        _phoneNumber.value = newValue
        // user zmienił numer -> anuluj stary licznik i odblokuj przycisk od razu
        stopCountdown()
    }

    private val _countryPrefix = MutableStateFlow(
        CountryPrefix(name = "Poland", prefix = 48, isoAlpha2 = "PL")
    )
    val countryPrefix: StateFlow<CountryPrefix> = _countryPrefix.asStateFlow()

    fun updateCountryPrefixData(countryPrefix: CountryPrefix) {
        _countryPrefix.value = countryPrefix
        // zmiana prefiksu to tez zmiana pełnego numeru -> anuluj licznik
        stopCountdown()
    }

    /** Wysyłka SMS (Firebase). Po sukcesie 'onCodeSent' ustawiamy cooldown dla tego pełnego numeru. */
    fun sendSms(fullPhone: String, activity: Activity) {
        viewModelScope.launch {
            _uiState.value = AuthSmsUiState.Loading
            repo.sendConfirmationSms(fullPhone, activity)
                .onSuccess { verificationId ->
                    _uiState.value = AuthSmsUiState.SuccessSend(verificationId)
                    // start cooldown DOPIERO po potwierdzeniu wysłania (onCodeSent)
                    setCooldown(fullPhone)
                }
                .onFailure { err ->
                    // oznacza ze user ma wlaczony twoFA ale probuje zalogowac sie samym numerem telefonu
                    if (mapError(err) == AuthError.FirstFactorRequired) {
                        _events.tryEmit(AuthSmsUiEvent.NavigateToSignInScreen)
                        Log.d(
                            "TWOFA",
                            "user ma wlaczony twoFA ale probuje zalogowac sie samym numerem telefonu"
                        )
                        _uiState.value = AuthSmsUiState.FistFactorReq
                    } else {
                        println(err)
                        _uiState.value = AuthSmsUiState.Failed(mapError(err))
                    }
                }
        }
    }

    /** Weryfikacja kodu. */
    fun verifySms(verificationId: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthSmsUiState.Loading
            repo.verifySmsCode(verificationId, code)
                .onSuccess { r ->
                    _uiState.value = AuthSmsUiState.Success(r.isNewUser)
                    // tu NIE dotykamy cooldownu – on już został ustawiony po wysłaniu SMS
                }
                .onFailure { err ->
                    println(err)
                    _uiState.value = AuthSmsUiState.Failed(mapError(err))
                }
        }
    }


    /** Zapis + start licznika. Wołane po udanym wysłaniu SMS (onCodeSent). */
    fun setCooldown(fullPhone: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dataStoreRepo.saveCooldownAndPhoneNumber(fullPhone, now)
            startCountdown(endTimeMs = now + cooldownSec * 1000)
        }
    }

    fun linkPhoneNumToEmail(smsCode: String, verificationId: String) {
        println("linkPhoneToCurrentUser start")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthSmsUiState.Loading
            repo.linkPhoneToCurrentUser(verificationId, smsCode)
                .onSuccess {
                    println("success linkPhoneToCurrentUser")
                    _events.tryEmit(
                        AuthSmsUiEvent.PhoneNumberSuccessfullyLinked(
                            "Your phone number has been successfully linked to your account. " +
                                    "This means your existing account (created with email and password) now also has phone sign-in enabled. " +
                                    "From now on, you can log in either with your email and password or by using this phone number " +
                                    "(after receiving an SMS verification code). Your account ID and data remain the same — we only added an " +
                                    "additional sign-in method for convenience and security."
                        )
                    )
                    userRepo.addPhoneNumberToFirestore(
                        _phoneNumber.value,
                        _countryPrefix.value.prefix.toString()
                    )
                }
                .onFailure { err ->
                    println(err)
                    _uiState.value = AuthSmsUiState.FailedLinking(err.toLinkPhoneError())
                }
        }
    }

    /** Odtwarza licznik z DataStore dla podanego pełnego numeru. */
    fun loadCooldown(fullPhone: String) {
        viewModelScope.launch {
            // najpierw anuluj poprzedni licznik i odblokuj przycisk natychmiast
            stopCountdown()
            val lastSentAt = dataStoreRepo.getCooldownStartForPhoneNumber(fullPhone)
            if (lastSentAt != null) {
                startCountdown(endTimeMs = lastSentAt + cooldownSec * 1000)
            } // jeśli null → brak wpisu → pozostaje 0
        }
    }

    /** Główny licznik – jeden job; liczy względem zegara. */
    private fun startCountdown(endTimeMs: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val remain = ((endTimeMs - System.currentTimeMillis()).coerceAtLeast(0) / 1000)
                _remainingSec.value = remain
                if (remain == 0L) break
                delay(1000)
            }
        }
    }

    /** Ręczne zatrzymanie odliczania i odblokowanie przycisku. */
    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _remainingSec.value = 0L
    }

    fun updateAuthSmsUiState(state: AuthSmsUiState) {
        _uiState.value = state
    }

    sealed class AuthSmsUiEvent {
        data class PhoneNumberSuccessfullyLinked(val msg: String) : AuthSmsUiEvent()
        data class EmailSuccessfullyLinked(val msg: String) : AuthSmsUiEvent()
        data object NavigateToSignInScreen : AuthSmsUiEvent()
    }

    sealed class AuthSmsUiState {
        data object Idle : AuthSmsUiState()
        data object Loading : AuthSmsUiState()
        data class SuccessSend(val verificationId: String) : AuthSmsUiState()
        data class Success(val isNew: Boolean) : AuthSmsUiState()
        data class Failed(val message: AuthError) : AuthSmsUiState()
        data object FistFactorReq : AuthSmsUiState()
        data class FailedLinking(val message: LinkPhoneError) : AuthSmsUiState()
    }

    enum class AuthError {
        InvalidPhoneNumber,
        TooManyRequests,
        InvalidVerificationCode,
        NetworkUnavailable,
        FirstFactorRequired,
        Unknown
    }

    private fun mapError(t: Throwable): AuthError {
        if (t is CancellationException) throw t

        return when (t) {
            is FirebaseNetworkException, is IOException ->
                AuthError.NetworkUnavailable

            is FirebaseTooManyRequestsException ->
                AuthError.TooManyRequests

            is FirebaseAuthInvalidCredentialsException -> {
                val code = (t as? FirebaseAuthException)?.errorCode
                when (code) {
                    "ERROR_INVALID_PHONE_NUMBER" -> AuthError.InvalidPhoneNumber
                    "ERROR_INVALID_VERIFICATION_CODE",
                    "ERROR_INVALID_CREDENTIAL" -> AuthError.InvalidVerificationCode

                    else -> AuthError.InvalidVerificationCode
                }
            }

            is FirebaseAuthException -> {
                val code = t.errorCode
                val msg = (t.message ?: "").lowercase()

                // "requires sign-in with a supported first factor"
                if ("supported first factor" in msg || "cannot be set as a first factor" in msg) {
                    return AuthError.FirstFactorRequired
                }

                when (code) {
                    else -> AuthError.Unknown
                }
            }

            else -> AuthError.Unknown
        }
    }
}


fun Throwable.toLinkPhoneError(): LinkPhoneError {
    if (this.message == "User not logged in") return LinkPhoneError.NotLoggedIn

    return when (this) {
        is FirebaseAuthInvalidCredentialsException -> {
            // czasem tu wpada zły kod / wygasła sesja
            when ((this as? FirebaseAuthException)?.errorCode) {
                "ERROR_SESSION_EXPIRED" -> LinkPhoneError.CodeExpired
                else -> LinkPhoneError.InvalidCode
            }
        }

        is FirebaseAuthUserCollisionException -> LinkPhoneError.PhoneAlreadyInUse
        is FirebaseTooManyRequestsException -> LinkPhoneError.TooManyRequests
        is FirebaseNetworkException -> LinkPhoneError.Network
        is FirebaseAuthException -> {
            // fallback po kodzie
            when (this.errorCode) {
                "ERROR_TOO_MANY_REQUESTS" -> LinkPhoneError.TooManyRequests
                "ERROR_SESSION_EXPIRED" -> LinkPhoneError.CodeExpired
                "ERROR_CREDENTIAL_ALREADY_IN_USE" -> LinkPhoneError.PhoneAlreadyInUse
                else -> LinkPhoneError.Unknown(this.message)
            }
        }

        else -> LinkPhoneError.Unknown(this.message)
    }
}

sealed interface LinkPhoneError {
    data object InvalidCode : LinkPhoneError
    data object CodeExpired : LinkPhoneError
    data object PhoneAlreadyInUse : LinkPhoneError
    data object NotLoggedIn : LinkPhoneError
    data object TooManyRequests : LinkPhoneError
    data object Network : LinkPhoneError
    data class Unknown(val message: String?) : LinkPhoneError
}
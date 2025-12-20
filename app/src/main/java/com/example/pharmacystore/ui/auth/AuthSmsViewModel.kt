package com.example.pharmacystore.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.data.datastore.DataStoreRepo
import com.example.pharmacystore.data.local.phoneprefixes.CountryPrefix
import com.example.pharmacystore.repo.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthSmsViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

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
                    _uiState.value = AuthSmsUiState.Failed(mapError(err))
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

    sealed class AuthSmsUiState {
        data object Idle : AuthSmsUiState()
        data object Loading : AuthSmsUiState()
        data class SuccessSend(val verificationId: String) : AuthSmsUiState()
        data class Success(val isNew: Boolean) : AuthSmsUiState()
        data class Failed(val message: Err) : AuthSmsUiState()
    }

    enum class Err { BAD_PHONE, TOO_MANY, NO_NETWORK, GENERIC }

    private fun mapError(t: Throwable): Err = when (t) {
        is FirebaseNetworkException, is IOException -> Err.NO_NETWORK
        is FirebaseAuthInvalidCredentialsException -> when (t.errorCode) {
            "ERROR_INVALID_PHONE_NUMBER" -> Err.BAD_PHONE
            else -> Err.GENERIC
        }
        is FirebaseTooManyRequestsException -> Err.TOO_MANY
        else -> Err.GENERIC
    }
}

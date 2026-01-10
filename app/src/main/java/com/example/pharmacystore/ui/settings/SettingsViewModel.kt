package com.example.pharmacystore.ui.settings

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.UserSettings
import com.example.pharmacystore.repo.AuthRepository
import com.example.pharmacystore.repo.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _isVerified = MutableStateFlow(false)
    val isVerified = _isVerified.asStateFlow()

    private val _events = MutableSharedFlow<SettingsScreenEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsScreenEvent> = _events

    private val _state = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Idle)
    val state = _state.asStateFlow()

    private val _reAuthState = MutableStateFlow<ReAuthState>(ReAuthState.Idle)
    val reAuthState = _reAuthState.asStateFlow()

    private val _twoFaState = MutableStateFlow<TwoFaState>(TwoFaState.Idle)
    val twoFaState = _twoFaState.asStateFlow()

    private val _twoFAEvents = MutableSharedFlow<TwoFaEvents>(extraBufferCapacity = 1)
    val twoFAEvents: SharedFlow<TwoFaEvents> = _twoFAEvents

    fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            _state.value = SettingsScreenState.Loading
            val res = userRepo.saveSettingsForUser(settings)
            res.onSuccess {
                _events.tryEmit(SettingsScreenEvent.ShowToastRefresh("Settings Saved"))
            }.onFailure { e ->
                if (e is CancellationException) throw e
                _events.tryEmit(SettingsScreenEvent.ShowToast("Something went wrong"))
                _state.value =
                    SettingsScreenState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    fun checkIfEmailIsVerified(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = authRepo.checkIfEmailIsVerified()
            res.onSuccess { value ->
                _isVerified.value = value
                onSuccess()
            }.onFailure { err ->
                println(err)
            }
        }
    }

    private suspend fun getPhoneNumber(): String? {
        val phoneNumber = userRepo.getPhoneNumber().getOrNull()
        return phoneNumber?.filter { it != ' '}
    }

    fun mfaEnrollment(activity: Activity) {
        viewModelScope.launch {
            val phoneNumber = getPhoneNumber() ?: return@launch
            val res = authRepo.mfaEnrollment(phoneNumber, activity)
            res.onSuccess { verificationId ->
                Log.e("TWOFA", "mfaEnrollment success, navigate to code screen")
                _twoFaState.value = TwoFaState.AwaitingCode(verificationId)
                _twoFAEvents.tryEmit(TwoFaEvents.NavigateToCodeScreen(verificationId))
            }.onFailure { e ->
                _twoFaState.value = TwoFaState.SmsError(e.localizedMessage ?: "Failed to send SMS.")

                Log.e("TWOFA", "Failed to send SMS.")
                Log.e("TWOFA", e.localizedMessage ?:"")
            }
        }
    }

    fun completeEnrollment(verificationId: String?, code: String) {
        viewModelScope.launch {
            if (verificationId == null) return@launch
            authRepo.completeEnrollment(verificationId, code)
                .onSuccess { // udalo sie zrobic 2fa
                    _twoFaState.value = TwoFaState.Enabled
                    Log.d("TWOFA", "2fa Enabled")
                }
                .onFailure { e ->
                    _twoFaState.value = TwoFaState.EnrollError(e.localizedMessage ?: "Invalid code")
                    Log.d("TWOFA", "2fa Error")
                }
        }
    }

    fun reAuth(password: String, activity: Activity) {
        viewModelScope.launch {
            authRepo.reAuth(password).fold(
                onSuccess = {
                    Log.d("AUTH", "success reAuth")
                },
                onFailure = { e ->

                    when (e) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            _reAuthState.value = ReAuthState.Error("Wrong Password")
                        }

                        is FirebaseNetworkException -> {
                            _reAuthState.value = ReAuthState.Error("No internet connection")
                        }

                        is FirebaseAuthException -> {
                            Log.e("AUTH", "Auth errorCode=${e.errorCode}", e)
                        }

                        else -> {
                            _reAuthState.value = ReAuthState.Error("Try again later")
                        }
                    }
                    _twoFAEvents.tryEmit(TwoFaEvents.ClearPassword)
                    Log.e("AUTH", "error reAuth")
                }
            )
            mfaEnrollment(activity)
        }
    }


    sealed interface SettingsScreenEvent {
        data class ShowToastRefresh(val msg: String) : SettingsScreenEvent
        data class ShowToast(val msg: String) : SettingsScreenEvent
    }

    sealed interface SettingsScreenState {
        data object Loading : SettingsScreenState
        data class Error(val msg: String) : SettingsScreenState
        data object Idle : SettingsScreenState
    }

    sealed interface TwoFaState {
        data object Idle : TwoFaState
//        data object CheckingPrerequisites : TwoFaState
//        data object NeedPassword : TwoFaState
//        data class ReAuthError(val msg: String) : TwoFaState

        data object ReAuthSuccess : TwoFaState

        //data object SendingSms : TwoFaState
        data class AwaitingCode(val verificationId: String) : TwoFaState
        data class SmsError(val msg: String) : TwoFaState

        //data object Enrolling : TwoFaState
        data object Enabled : TwoFaState
        data class EnrollError(val msg: String) : TwoFaState

        // 1. Zrobic reAuth
        // 2. mfaEnrollment
        // 3. completeEnrollment
    }

    sealed interface TwoFaEvents {
        data object ClearPassword : TwoFaEvents
        data class NavigateToCodeScreen(val verificationId: String) : TwoFaEvents
        data object Idle : TwoFaEvents
    }


    sealed interface ReAuthState {
        data class Error(val msg: String) : ReAuthState
        data object Idle : ReAuthState
    }
}
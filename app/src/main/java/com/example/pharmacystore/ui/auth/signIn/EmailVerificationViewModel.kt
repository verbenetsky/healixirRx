package com.example.pharmacystore.ui.auth.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.data.datastore.DataStoreRepo
import com.example.pharmacystore.repo.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dataStoreRepo: DataStoreRepo,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val cooldownSec = 60L
    private var countdownJob: Job? = null

    private val _remainingSec = MutableStateFlow(0L)
    val remainingSec = _remainingSec.asStateFlow()

    private val _emailVerifiedState = MutableStateFlow<EmailVerifiedState>(EmailVerifiedState.Unknown)
    val emailVerifiedState = _emailVerifiedState.asStateFlow()

    fun refreshEmailVerified() = viewModelScope.launch {
        _emailVerifiedState.value = EmailVerifiedState.Unknown
        val state = repo.checkIfEmailIsVerified()
            .fold(
                onSuccess = { if (it) EmailVerifiedState.Verified else EmailVerifiedState.NotVerified },
                onFailure = { EmailVerifiedState.Error(it.message ?: "Unknown error") }
            )
        _emailVerifiedState.value = state
    }

    fun sendVerificationEmail() = viewModelScope.launch {
        repo.sendEmailVerification()
            .onSuccess {
                val email = auth.currentUser?.email ?: return@onSuccess
                setCoolDown(email)
            }
            .onFailure {

            }
    }

    fun loadCooldown() {
        stopCountdown()
        val email = auth.currentUser?.email ?: return
        viewModelScope.launch {
            val lastSentAt = dataStoreRepo.getCooldownAndEmail(email) ?: return@launch
            startCountdown(lastSentAt + cooldownSec * 1000)
        }
    }

    private fun setCoolDown(email: String) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            dataStoreRepo.saveCooldownAndEmail(email, now)
            startCountdown(now + cooldownSec * 1000)
        }
    }

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

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _remainingSec.value = 0L
    }

    sealed interface EmailVerifiedState {
        data object Unknown : EmailVerifiedState
        data object Verified : EmailVerifiedState
        data object NotVerified : EmailVerifiedState
        data class Error(val msg: String) : EmailVerifiedState
    }
}

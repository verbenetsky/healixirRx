package com.example.pharmacystore.ui.auth.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.Validation
import com.example.pharmacystore.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class EmailPasswordSignUpViewModel @Inject constructor(private val repo: AuthRepository) :
    ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _validationState = MutableStateFlow(Validation())
    val validationState = _validationState.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState = _authUiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvents>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _validationState.update { currentState ->
            currentState.copy(
                email = (validateEmail(newEmail))
            )
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repo.signUpUser(email, password)
            result
                .onSuccess {
                    _events.tryEmit(AuthEvents.NavigateToProfileSetUpScreen)
                }.onFailure { err ->
                    println(err.localizedMessage ?: "Unknown error")
                    _authUiState.value = AuthUiState.Error(err.localizedMessage ?: "Unknown error")
                }
        }
    }

    val emailPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    fun validateEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    private val passwordPattern =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,}$")

    fun validatePassword(password: String) {
        _validationState.update { currentState ->
            currentState.copy(
                password = passwordPattern.matcher(
                    password
                ).matches()
            )
        }
    }

    fun resetAuthState() {
        _authUiState.value = AuthUiState.Idle
    }

    sealed interface AuthUiState {
        data object Idle : AuthUiState
        data object Loading : AuthUiState
        data class Error(val error: String) : AuthUiState // blad rejestracji
    }

    sealed interface AuthEvents {
        data object NavigateToMainScreen : AuthEvents
        data object NavigateToProfileSetUpScreen : AuthEvents
    }
}
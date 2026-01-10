package com.example.pharmacystore.ui.auth.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.Validation
import com.example.pharmacystore.repo.AuthRepository
import com.example.pharmacystore.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class EmailPasswordSignUpViewModel @Inject constructor(
    private val repo: AuthRepository,
    val userRepo: UserRepository
) :
    ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _validationState = MutableStateFlow(Validation())
    val validationState = _validationState.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState = _authUiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvents>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun linkEmail(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.linkEmail(email, password)
                .onSuccess {
                    println("success linkPhoneToCurrentUser")
                    _events.tryEmit(
                        AuthEvents.EmailSuccessfullyLinked(
                            "Your email has been successfully linked to your account. " +
                                    "This means your existing account (created with phone number) now also has email+password sign-in enabled. " +
                                    "From now on, you can log in either with your email and password or by using your phone number." +
                                    "Your account ID and data remain the same — we only added an " +
                                    "additional sign-in method for convenience and security."
                        )
                    )
                    userRepo.addEmailToFirestore(email)
                }
                .onFailure { err ->
                    println(err)
                }
        }
    }

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

    private val emailPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    private fun validateEmail(email: String): Boolean {
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
        data object NavigateToMainScreen: AuthEvents
        data object NavigateToProfileSetUpScreen: AuthEvents
        data class EmailSuccessfullyLinked(val msg: String): AuthEvents
    }
}
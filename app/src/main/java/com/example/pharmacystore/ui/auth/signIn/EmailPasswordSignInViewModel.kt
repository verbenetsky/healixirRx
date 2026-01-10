package com.example.pharmacystore.ui.auth.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.Validation
import com.example.pharmacystore.repo.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
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
class EmailPasswordSignInViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) :
    ViewModel() {
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)

    // jesli null to jesze sie nie zaladowalo
    val isLoggedIn = _isLoggedIn.asStateFlow()

    // rowniez trzeba sprawdzic czy user przypadkiem nie zapomniaj ukonczyc profileSetUp'u
    // bez tego to jesli nie sprawdzic to na ekranie profileSetUp wyjsc z apki i wejsc z powrotem to odrazu przeniesie do
    // home screen i w profile beda puste dane
    init {
        _isLoggedIn.value = firebaseAuth.currentUser != null
    }

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState = _authUiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _validationState = MutableStateFlow(Validation())
    val validationState = _validationState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _validationState.update { currentState ->
            currentState.copy(
                email = (validateEmail(newEmail))
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repo.signInUser(email, password)
            println("repo sign in")
            result.onSuccess {
                println("success")
                _events.tryEmit(AuthEvent.NavigateToMainScreen)
            }.onFailure { err ->
                println(err.localizedMessage ?: "Unknown error")
                _authUiState.value = AuthUiState.Error(err.toAuthError().userMessage())
                _events.tryEmit(AuthEvent.Error())
            }
        }
    }

    fun logOut(onSuccess: () -> Unit) {
        firebaseAuth.signOut()
        onSuccess()
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


    sealed interface AuthUiState {
        data object Idle : AuthUiState
        data object Loading : AuthUiState
        data class Error(val error: String) : AuthUiState // blad logowania
    }

    sealed interface AuthEvent {
        data class Error(val msg: String = "") : AuthEvent
        data object NavigateToMainScreen : AuthEvent
    }

    sealed interface AuthError {
        data object InvalidCredentials : AuthError
        data object Network : AuthError
        data object Unknown : AuthError
        data object TooManyRequest : AuthError
    }


    fun Throwable.toAuthError(): AuthError {

        if (this is FirebaseTooManyRequestsException) return AuthError.TooManyRequest

        // sieć
        if (this is FirebaseNetworkException) return AuthError.Network

        // najczęstszy przypadek przy email/password
        if (this is FirebaseAuthInvalidCredentialsException) return AuthError.InvalidCredentials

        // fallback po errorCode (czasem jest ERROR_INVALID_CREDENTIAL)
        val code = (this as? FirebaseAuthException)?.errorCode
        if (code == "ERROR_INVALID_CREDENTIAL") return AuthError.InvalidCredentials

        // fallback po message (jeśli naprawdę tylko to widzisz)
        val msg = this.message.orEmpty()
        if (msg.contains(
                "The supplied auth credential is incorrect, malformed or has expired",
                ignoreCase = true
            )
        ) {
            return AuthError.InvalidCredentials
        }

        return AuthError.Unknown
    }

    fun AuthError.userMessage(): String = when (this) {
        AuthError.InvalidCredentials -> "Invalid email or password."
        AuthError.Network -> "Network error. Check your connection."
        AuthError.Unknown -> "Something went wrong. Please try again."
        AuthError.TooManyRequest -> "Too many attempts. Try again later"
    }
}
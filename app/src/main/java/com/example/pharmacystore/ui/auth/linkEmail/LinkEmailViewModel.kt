package com.example.pharmacystore.ui.auth.linkEmail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.Validation
import com.example.pharmacystore.repo.AuthRepository
import com.example.pharmacystore.repo.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
class LinkEmailViewModel @Inject constructor(
    private val repo: AuthRepository,
    val userRepo: UserRepository
) : ViewModel() {

    private val _linkEmailState = MutableStateFlow<LinkEmailState>(LinkEmailState.Idle)
    val linkEmailState = _linkEmailState.asStateFlow()

    private val _linkEmailEvents =
        MutableSharedFlow<LinkEmailEvent>(replay = 0, extraBufferCapacity = 1)
    val linkEmailEvents = _linkEmailEvents.asSharedFlow()

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


    fun linkEmail(email: String, password: String) {
        viewModelScope.launch {
            _linkEmailState.value = LinkEmailState.Loading
            repo.linkEmail(email, password)
                .onSuccess {
                    println("success linkPhoneToCurrentUser")
                    _linkEmailEvents.tryEmit(
                        LinkEmailEvent.EmailSuccessfullyLinked(
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
                    _linkEmailState.value = LinkEmailState.Error(err.toLinkEmailMessage())
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

    sealed interface LinkEmailState {
        data object Idle : LinkEmailState
        data object Loading : LinkEmailState
        data class Error(val error: String) : LinkEmailState
    }

    sealed interface LinkEmailEvent {
        data class EmailSuccessfullyLinked(val msg: String) : LinkEmailEvent
    }


    fun Throwable.toLinkEmailMessage(): String {
        // Najczęstsze przypadki dla linkWithCredential(email/pass)
        return when (this) {
            is FirebaseAuthUserCollisionException -> {
                // Email/credential już jest przypisany do innego konta
                "This email is already used by another account. Please sign in with that email first, then link your phone number to that account."
            }

            is FirebaseAuthWeakPasswordException -> {
                "Your password is too weak. Use at least 8 characters, including upper/lowercase letters, a number, and a special character."
            }

            is FirebaseAuthInvalidCredentialsException -> {
                // często: zły format emaila albo inne invalid credentials
                "Invalid email address or password. Please check and try again."
            }

            is FirebaseNetworkException -> {
                "No internet connection. Please check your network and try again."
            }

            is IllegalArgumentException -> {
                // z require(...) w repo (blank email/pwd)
                message?.takeIf { it.isNotBlank() } ?: "Invalid input. Please try again."
            }

            is FirebaseAuthException -> {
                // Fallback po errorCode, przydatne gdy trafisz na mniej typowy błąd
                when (errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already used by another account."
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "This sign-in method is already linked to another account."
                    "ERROR_INVALID_EMAIL" -> "Invalid email address."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is disabled for this project."
                    "ERROR_REQUIRES_RECENT_LOGIN" -> "For security reasons, please re-authenticate and try again."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait a moment and try again."
                    else -> message?.takeIf { it.isNotBlank() }
                        ?: "Authentication error. Please try again."
                }
            }

            else -> {
                // Ostateczny fallback
                message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
            }
        }
    }

}
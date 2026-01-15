package com.example.pharmacystore.ui.auth.signIn

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.Validation
import com.example.pharmacystore.repo.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.MultiFactorResolver
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


    // -------------------------MFA-----------------------------------------------------------------
    // niestety musi to tutaj byc choc i wyglada nie dokonca ladnie
    private val _mfaState = MutableStateFlow<MfaState>(MfaState.Idle)
    val mfaState = _mfaState.asStateFlow()

    private val _mfaEvents = MutableSharedFlow<MfaEvents>(replay = 0, extraBufferCapacity = 1)
    val mfaEvents = _mfaEvents.asSharedFlow()

    private val _resolver = MutableStateFlow<MultiFactorResolver?>(null)
    val resolver = _resolver.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId = _verificationId.asStateFlow()

    // ---------------------------------------------------------------------------------------------

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

    fun signIn(email: String, password: String, activity: Activity) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repo.signInUser(email, password)
            println("repo sign in")
            result.onSuccess {
                println("success")
                _events.tryEmit(AuthEvent.NavigateToMainScreen)
            }.onFailure { err ->
                // jesli dostajemy FirebaseAuthMultiFactorException to znaczy ze do konkretnego tego emaila i hasla jest
                // jest podpiety twoFA

                if (err is FirebaseAuthMultiFactorException) {
                    _resolver.value = err.resolver
                    // odrazu wysylamy kod userowi
                    sendMfaSms(activity)
                } else {
                    println(err)
                    println(err.localizedMessage ?: "Unknown error")
                    _authUiState.value = AuthUiState.Error(err.toAuthError().userMessage())
                    _events.tryEmit(AuthEvent.Error())
                }
            }
        }
    }

    fun sendMfaSms(activity: Activity) {
        _authUiState.value = AuthUiState.Idle
        if (_resolver.value == null) {
            Log.d("TWOFA", "Resolver is null")
            return
        }
        viewModelScope.launch {
            _mfaState.value = MfaState.Loading
            val res = repo.sendSmsCodeMfaSignIn(_resolver.value!!, activity)
            res.onSuccess { verificationId ->

                _events.tryEmit(AuthEvent.NavigateToMfaSmsCodeScreen)

                _verificationId.value = verificationId

                _mfaState.value = MfaState.MfaSmsCodeSent

                Log.d("TWOFA", "Code successfully sent")

            }.onFailure { err ->
                _authUiState.value = AuthUiState.Error(mapSmsCodeError(err))
                Log.d("TWOFA", "Error: ${err.localizedMessage ?: "error "}")
            }
        }
    }

    fun verifyMfaSmsCode(code: String, verificationId: String?) {
        if (_resolver.value == null || _verificationId.value == null) {
            Log.d("TWOFA", "Resolver or verification id is null")
            return
        }
        viewModelScope.launch {
            _mfaState.value = MfaState.Loading
            val r = repo.verifySmsCodeMfaSignIn(code, verificationId!!, _resolver.value!!)
            r.onSuccess {
                _mfaEvents.tryEmit(MfaEvents.NavigateToMain)
            }.onFailure { err ->
                _mfaState.value = MfaState.Error(mapSmsCodeError(err))
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

    sealed interface MfaState {
        data object Idle : MfaState
        data object Loading : MfaState
        data object MfaSmsCodeSent : MfaState
        data class Error(val msg: String) : MfaState
    }

    sealed interface MfaEvents {
        data object NavigateToMain : MfaEvents
    }

    sealed interface AuthUiState {
        data object Idle : AuthUiState
        data object Loading : AuthUiState
        data class MfaCodeRequired(val verId: String) : AuthUiState
        data class Error(val error: String) : AuthUiState // blad logowania
    }

    sealed interface AuthEvent {
        data class Error(val msg: String = "") : AuthEvent
        data object NavigateToMainScreen : AuthEvent
        data object NavigateToMfaSmsCodeScreen : AuthEvent
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

    private fun mapSmsCodeError(t: Throwable): String {
        // Nie maskuj anulowania korutyn
        if (t is kotlinx.coroutines.CancellationException) throw t

        // Sieć / limity
        if (t is FirebaseNetworkException) {
            return "No internet connection. Please check your network and try again."
        }
        if (t is FirebaseTooManyRequestsException) {
            return "Too many attempts. Please wait a moment and try again."
        }

        // Najczęstsze dla SMS/MFA: zły kod / wygasły kod / wygasła sesja
        val authEx = t as? FirebaseAuthException
        val code = authEx?.errorCode

        return when {
            // Zły kod / niewłaściwe credentiale SMS
            t is FirebaseAuthInvalidCredentialsException ||
                    code == "ERROR_INVALID_VERIFICATION_CODE" ||
                    code == "ERROR_INVALID_CREDENTIAL" -> {
                "Incorrect verification code. Please check the SMS and try again."
            }

            // Wygasły kod / wygasła sesja weryfikacji
            code == "ERROR_CODE_EXPIRED" ||
                    code == "ERROR_SESSION_EXPIRED" -> {
                "The verification code has expired. Request a new code and try again."
            }

            // Użytkownik / sesja
            t is FirebaseAuthInvalidUserException ||
                    code == "ERROR_USER_DISABLED" ||
                    code == "ERROR_USER_NOT_FOUND" -> {
                "This account is no longer available. Please sign in again."
            }

            // Gdy resolver/flow MFA jest niekompletny albo stan jest nieprawidłowy
            t is FirebaseAuthMultiFactorException -> {
                "Additional verification is required. Please try signing in again."
            }

            // Fallback: pokaż sensowny tekst
            else -> {
                t.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Something went wrong. Please try again."
            }
        }
    }


    fun AuthError.userMessage(): String = when (this) {
        AuthError.InvalidCredentials -> "Invalid email or password."
        AuthError.Network -> "Network error. Check your connection."
        AuthError.Unknown -> "Something went wrong. Please try again."
        AuthError.TooManyRequest -> "Too many attempts. Try again later"
    }
}
package com.example.pharmacystore.ui.auth.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pharmacystore.common.isEmail
import com.example.pharmacystore.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {

    private val _events =
        MutableSharedFlow<AuthGateEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _state = MutableStateFlow<AuthGateState>(AuthGateState.Loading)
    val state = _state.asStateFlow()

    private var listenJob: Job? = null
    private var navigationSent: Boolean = false

    fun startListenForUser(uid: String) {
        listenJob?.cancel()
        navigationSent = false
        _state.value = AuthGateState.Loading

        listenJob = viewModelScope.launch {
            repo.listenForUser(uid)
                .map { user ->
                    val contact = user.email?.takeIf { it.isNotBlank() }
                        ?: user.phoneNumber?.takeIf { it.isNotBlank() }

                    val type = contact?.let { isEmail(it) } // "email" / "phoneNumber" / null
                    Triple(user.configurationCompleted, type, contact)
                }
                .distinctUntilChanged()
                .catch { e ->
                    navigationSent = false
                    _state.value = AuthGateState.Error(e.message ?: "No internet connection")
                }
                .collect { (completed, type, contact) ->
                    if (navigationSent) return@collect
                    navigationSent = true

                    if (completed) {
                        _events.tryEmit(AuthGateEvent.NavigateToHomeScreen)
                    } else {
                        _events.tryEmit(
                            AuthGateEvent.NavigateToProfileSetUp(type to contact)
                        )
                    }

                    // ważne: kończymy słuchanie po podjęciu decyzji, żeby nie emitować ponownie
                    listenJob?.cancel()
                    listenJob = null
                }
        }
    }

    /**
     * Zostawiam tę metodę, żebyś nie musiał ruszać Composable (masz ją wywoływaną z onCheckIfSetUpCompleted).
     * Jeśli już słuchamy usera (startListenForUser), to nie rób drugi raz.
     *
     * Jeżeli chcesz tu “one-shot” bez listenera, to daj znać — dopasuję do repo.
     */

    fun decideWhereToGo(uid: String) {
        // jeśli listener już działa, nie dubluj
        if (listenJob?.isActive == true) return
        startListenForUser(uid)
    }

    fun retry(uid: String) {
        startListenForUser(uid)
    }

    public override fun onCleared() {
        listenJob?.cancel()
        listenJob = null
        super.onCleared()
    }

    sealed interface AuthGateEvent {
        data class NavigateToProfileSetUp(val contact: Pair<String?, String?>) : AuthGateEvent
        data object NavigateToHomeScreen : AuthGateEvent
    }

    sealed interface AuthGateState {
        data object Loading : AuthGateState
        data class Error(val msg: String) : AuthGateState
    }
}

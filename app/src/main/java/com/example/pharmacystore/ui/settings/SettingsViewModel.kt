package com.example.pharmacystore.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.UserSettings
import com.example.pharmacystore.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SettingsViewModel @Inject constructor(private val userRepo: UserRepository) :
    ViewModel() {

    private val _events = MutableSharedFlow<SettingsScreenEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsScreenEvent> = _events

    private val _state = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Idle)
    val state = _state.asStateFlow()

    fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            _state.value = SettingsScreenState.Loading
            val res = userRepo.saveSettingsForUser(settings)
            res.onSuccess {
                onSaveSuccess()
            }.onFailure { e ->
                if (e is CancellationException) throw e
                onSaveError()
                _state.value =
                    SettingsScreenState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    private fun onSaveSuccess() {
        _events.tryEmit(SettingsScreenEvent.ShowToastRefresh("Settings Saved"))
    }

    private fun onSaveError() {
        _events.tryEmit(SettingsScreenEvent.ShowToast("Something went wrong"))
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
}
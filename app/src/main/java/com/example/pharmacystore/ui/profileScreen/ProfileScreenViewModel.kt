package com.example.pharmacystore.ui.profileScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.domain.model.UserInformationModel
import com.example.pharmacystore.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val userRepo: UserRepository,
) :
    ViewModel() {

    private val _userData = MutableStateFlow<UserInformationModel?>(null)
    val userData = _userData.asStateFlow()

    private val _uiState = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Idle)
    val uiState: StateFlow<ProfileScreenState> = _uiState.asStateFlow()

    init {
        getUser()
    }

    private fun getUser() {
        println("view model get user")
        viewModelScope.launch {
            _uiState.value = ProfileScreenState.Loading
            val res = userRepo.getUser()
            res.onSuccess { data ->
                _userData.update { data }
                _uiState.value = ProfileScreenState.Idle
            }.onFailure { e ->
                if (e is CancellationException) throw e   // ⟵ nie „zjada” anulowania
                _uiState.value = ProfileScreenState.Error(e.message ?: "Error")
            }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            val res = userRepo.getUser()
            res.onSuccess { data ->
                _userData.update { data }
            }
        }
    }

    fun returnRows(): List<InfoRowData> {
        val rows: List<InfoRowData> = listOfNotNull(
            _userData.value?.email?.takeIf { it.isNotBlank() }?.let {
                InfoRowData(Icons.Outlined.Email, "E-mail", it)
            },
            _userData.value?.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                InfoRowData(Icons.Outlined.Phone, "Phone number", it)
            }
        )
        return rows
    }

    fun returnMissing(
        onMissingEmailClick: () -> Unit,
        onMissingPhoneNumClick: () -> Unit
    ): MissingField {
        return if (_userData.value?.email == null)
            MissingField.Email(onMissingEmailClick)
        else
            MissingField.Phone(onMissingPhoneNumClick)
    }

    sealed interface ProfileScreenState {
        data object Idle : ProfileScreenState
        data object Loading : ProfileScreenState
        data class Error(val msg: String) : ProfileScreenState
    }

}
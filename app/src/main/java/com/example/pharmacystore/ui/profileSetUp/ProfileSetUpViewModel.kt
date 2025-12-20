package com.example.pharmacystore.ui.profileSetUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacystore.data.local.dao.CitiesVillagesDAO
import com.example.pharmacystore.domain.model.StreetsModel
import com.example.pharmacystore.data.local.dao.StreetsDAO
import com.example.pharmacystore.domain.model.CitiesVillagesModel
import com.example.pharmacystore.domain.model.UserInformationModel
import com.example.pharmacystore.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetUpViewModel @Inject constructor(
    private val streetsDAO: StreetsDAO,
    private val citiesVillagesDAO: CitiesVillagesDAO,
    private val userRepository: UserRepository
) : ViewModel() {

    // zmienna przechowuje laczna informacje o userze zeby potem z tych danych zrobic konto w firestore
    private val _userInformation: MutableStateFlow<UserInformationModel> =
        MutableStateFlow(UserInformationModel())
    val userInformation = _userInformation.asStateFlow()

    // zmienna ktora przechowuje liste miast/wsi do wyboru
    private val _citiesVillagesDetails: MutableStateFlow<List<CitiesVillagesModel>> =
        MutableStateFlow(emptyList())
    val citiesVillagesDetails = _citiesVillagesDetails.asStateFlow()

    // zmienna ktora przechowuje liste ulic do wyboru
    private val _streetsDetails: MutableStateFlow<List<StreetsModel>> =
        MutableStateFlow(emptyList())
    val streetsDetails = _streetsDetails.asStateFlow()

    private val _hasStreets: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val hasStreets = _hasStreets.asStateFlow()

    private val _uiState = MutableSharedFlow<UiEventAuth>()
    val uiState = _uiState.asSharedFlow()

    fun findSimilarCitiesOrVillages(name: String) {
        viewModelScope.launch(Dispatchers.IO) { // musi byc IO bo by default na main thread uruchamia sie launch co rzuci nam blad
            _citiesVillagesDetails.value = citiesVillagesDAO.getCityOrVillageByName(name)
            println(_citiesVillagesDetails.value)
            println(citiesVillagesDAO.countAll())
            println(citiesVillagesDAO.firstNames())
        }
    }

    fun hasStreets(sym: String) {
        viewModelScope.launch {
            _hasStreets.value = citiesVillagesDAO.hasStreets(sym)
        }
    }

    fun findSimilarStreet(name: String, sym: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _streetsDetails.value = streetsDAO.getStreetBySymAndName(name, sym)
        }
    }

    // function type with receiver
    fun updateUser(block: UserInformationModel.() -> UserInformationModel) {
        _userInformation.update { it.block() }
    }

//    fun updateUser(block: (UserInformationModel) -> UserInformationModel) {
//        _userInformation.update { block(it) }
//    }

    //<ReceiverType>.() -> <ReturnType>
    //ReceiverType – obiekt, który staje się this wewnątrz lambdy. Tu: UserInformation.

    fun saveUser(userInformation: UserInformationModel) {
        viewModelScope.launch {
            try {

                val map = buildMap<String, Any> {
                    // pola zawsze (nie-null)
                    put("userId", userInformation.userId)
                    put("nameSurname", userInformation.nameSurname)
                    put("city", userInformation.city)
                    put("woj", userInformation.woj)
                    put("powiat", userInformation.powiat)
                    put("houseNumber", userInformation.houseNumber)
                    put("configurationCompleted", true)
                    userInformation.birthday?.let { put("birthday", it) }

                    if (!userInformation.email.isNullOrBlank()) {
                        put("email", userInformation.email)
                    }

                    if (!userInformation.phoneNumber.isNullOrBlank()) {
                        put("phoneNumber", userInformation.phoneNumber)
                    }

                    if (!userInformation.street.isNullOrBlank()) {
                        put("street", userInformation.street)
                    }
                }

                userRepository.saveProfile(map)
                _uiState.emit(UiEventAuth.Success)
            } catch (e: Exception) {
                _uiState.emit(UiEventAuth.Error(e.localizedMessage ?: "error"))
            }
        }
    }

    // metoda ktora automatycznie stworzy dokument w firestore po rejesttracji
    fun onEnterScreen(phoneNumber: String?, email: String?) {
        viewModelScope.launch {
            try {
                userRepository.ensureUserDocExist(phoneNumber, email)
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    sealed interface UiEventAuth {
        data object Success : UiEventAuth
        data object Idle : UiEventAuth
        data class Error(val msg: String) : UiEventAuth
    }

}

//StateFlow zapamiętuje ostatni stan – świetne dla pól formularza, list,
//progresu, ale nie dla jednorazowych akcji.
//
//SharedFlow bez replay nie pamięta nic – idealne do wysyłania zdarzeń,
//które mają się   zdarzyć raz i zniknąć (nawigacja, Snackbar, dialog).
package com.example.pharmacystore.repo

import com.example.pharmacystore.domain.model.UserInformationModel
import com.example.pharmacystore.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // metoda ktora nadpisze istniejacy dokument i doda do niego nowe dane usera
    suspend fun saveProfile(userData: Map<String, Any>)

    // metoda odpowiadajaca za stworzenie dokumentu odrazu po tym jak pojawi sie konto w firebase auth
    suspend fun ensureUserDocExist(phoneNumber: String?, email: String?)

    suspend fun listenForUser(uid: String): Flow<UserInformationModel>

    // pobieranie usera dla profile Screena
    suspend fun getUser(): Result<UserInformationModel>

    suspend fun getPhoneNumber(): Result<String>

    suspend fun saveSettingsForUser(settings: UserSettings): Result<Unit>

    suspend fun getUserProfileSetUpCompleted(uid: String): Result<Boolean>

    suspend fun addPhoneNumberToFirestore(phoneNumber: String, prefix: String)
    suspend fun addEmailToFirestore(email: String)
}
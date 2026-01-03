package com.example.pharmacystore.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserInformationModel(
    val email: String? = null,
    val phoneNumber: String? = null,
    val userId: String = "",
    val nameSurname: String = "",
    val city: String = "",
    val woj: String = "",
    val powiat: String = "",
    val street: String? = null,
    val houseNumber: String = "",
    val birthday: Long? = null,
    val configurationCompleted: Boolean = false,
    val settings: UserSettings = UserSettings(),

    // serwer firestore sam wstawi czas serwera
    @ServerTimestamp
    val createdAt: Date? = null
)


data class UserSettings(
    val twoFactorEnabled: Boolean = false,
    val enableLinking: Boolean = false
)

// Warto wspomnieć o konwencji JavaBeans: mapper Firestore jej używa.
// Jeśli właściwość w modelu nazywa się `isConfigurationCompleted`,
// to Firestore szuka pola o nazwie `configurationCompleted` (bez "is").
// Dlatego pole `isConfigurationCompleted` w dokumencie nie będzie
// zmapowane automatycznie.

// nazwa isConfigurationCompleted() jest zarezerwowana dla gettera
// setConfigurationCompleted() dla settera
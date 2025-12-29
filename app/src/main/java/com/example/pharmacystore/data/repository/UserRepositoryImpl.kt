package com.example.pharmacystore.data.repository

import com.example.pharmacystore.domain.model.UserInformationModel
import com.example.pharmacystore.domain.model.UserSettings
import com.example.pharmacystore.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val uid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Not logged In")

    override suspend fun addPhoneNumberToFirestore(phoneNumber: String) {
        firestore
            .collection("users")
            .document(uid)
            .update("phoneNumber", phoneNumber)
            .await()
    }

    override suspend fun getUserProfileSetUpCompleted(uid: String): Result<Boolean> = runCatching {
        val snap = firestore.collection("users").document(uid).get().await()
        if (!snap.exists()) return@runCatching false
        (snap.get("configurationCompleted") ?: false) as Boolean
    }



    // nadpisuje obecny dokument dodajac do niego nowe dane
    override suspend fun saveProfile(userData: Map<String, Any>) {
        firestore.collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .await()
    }

    override suspend fun getUser(): Result<UserInformationModel> =
        runCatching {

            println("repo get user")

            require(uid.isNotBlank()) { "uid is blank" }

            val doc = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            check(doc.exists()) { "User not found" }

            doc.toObject(UserInformationModel::class.java)
                ?: error("There is no data for this user")
        }.onFailure { e ->
            if (e is CancellationException) throw e
        }

    override suspend fun saveSettingsForUser(settings: UserSettings): Result<Unit> = runCatching {
        require(uid.isNotBlank()) { "uid is blank" }

        firestore.collection("users")
            .document(uid)
            .update("settings", settings)
            .await()

        Unit
    }.onFailure { e ->
        if (e is CancellationException) throw e
    }


    // calkiem mozliwe ze uzycie Transaction jest troche overkill gdyz raczej nigdy nie wystapi sytuacji gdzie
// bedzie rownolegly zapis tego samego dokumentu z dwoch miejsc (dwa rozne urzadzenia)
    override suspend fun ensureUserDocExist(phoneNumber: String?, email: String?) {
        val ref = firestore.collection("users").document(uid)
        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            if (!snap.exists()) {
                if (phoneNumber != null) {
                    tx.set(
                        ref, mapOf(
                            UserInformationModel::phoneNumber.name to phoneNumber,
                            UserInformationModel::configurationCompleted.name to false,
                            UserInformationModel::createdAt.name to FieldValue.serverTimestamp()
                        )
                    )
                } else { // jesli email nie jest nullem
                    tx.set(
                        ref, mapOf(
                            UserInformationModel::email.name to email, // jest to referencja, .name zwraca String
                            UserInformationModel::configurationCompleted.name to false,
                            UserInformationModel::createdAt.name to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
        }.await()
    }

    override suspend fun listenForUser(uid: String): Flow<UserInformationModel> = callbackFlow {

        val l1 = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                if (snap == null) return@addSnapshotListener

                if (snap.metadata.hasPendingWrites()) return@addSnapshotListener

                val doc = snap.toObject(UserInformationModel::class.java)

                if (doc != null) {
                    trySend(doc)
                }
            }

        awaitClose {
            l1.remove()
        }
    }.conflate() // pomija stane posrednie i przepuszcza tylko najnowsza wartosc
        .distinctUntilChanged() // jeśli nowa wartość jest równa poprzedniej ( wedlug equals() ), nie zostanie wyemitowana

}


package com.example.contactappfirebase.data

import android.content.ContentValues
import android.util.Log
import com.example.contactappfirebase.model.Kontak
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

interface KontakRepository  {
    fun getAll(): Flow<List<Kontak>>
    suspend fun save(kontak: Kontak): String
    suspend fun update(kontak: Kontak): String
    suspend fun delete(kontakId: String): String
    fun getKontakById(kontakId: String): Flow<Kontak>
}

class KontakRepositoryImpl(private val firestore: FirebaseFirestore): KontakRepository{
    override fun getAll(): Flow<List<Kontak>> = flow {
        val snapshot = firestore.collection("Kontak")
            .orderBy("nama", Query.Direction.ASCENDING)
            .get()
            .await()
        val kontak = snapshot.toObjects(Kontak::class.java)
        emit(kontak)
    }.flowOn(Dispatchers.IO)

    override fun getKontakById(kontakId: String): Flow<Kontak> {
        return flow {
            val snapshot = firestore.collection("Kontak").document(kontakId).get().await()
            val kontak = snapshot.toObject(Kontak::class.java)
            emit(kontak!!)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun save(kontak: Kontak): String {
        return try {
            val documentReference = firestore.collection("Kontak")
                .add(kontak)
                .await()
            firestore.collection("Kontak").document(documentReference.id)
                .set(kontak.copy(id = documentReference.id))
            "Berhasil + ${documentReference.id}"
        } catch (e: Exception) {
            Log.w(ContentValues.TAG, "Error adding document", e)
            "Gagal: $e"
        }
    }

    override suspend fun delete(kontakId: String): String {
        firestore.collection("Kontak")
            .document(kontakId)
            .delete()
            .await()
        return "Delete Succes"
    }

    override suspend fun update(kontak: Kontak): String {
        firestore.collection("Kontak")
            .document(kontak.id)
            .set(kontak)
            .await()
        return "Update Success"
    }
}
package com.example.petdetective.data.repository

import com.example.petdetective.data.model.PetReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PetRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionRef = firestore.collection("pet_reports")

    val allReports: Flow<List<PetReport>> = callbackFlow {
        val listener = collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { doc ->
                        PetReport.fromDocument(doc.id, doc.data)
                    }
                    trySend(reports)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(report: PetReport) {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
        collectionRef.document(report.id).set(report.toMap()).await()
    }

    suspend fun delete(report: PetReport) {
        collectionRef.document(report.id).delete().await()
    }
}
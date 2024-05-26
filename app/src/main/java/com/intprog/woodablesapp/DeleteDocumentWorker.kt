package com.intprog.woodablesapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class DeleteDocumentWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val documentId = inputData.getString("documentId")
        val db = FirebaseFirestore.getInstance()
        if (documentId != null) {
            db.collection("assessment").document(documentId)
                .delete()
                .addOnSuccessListener { aVoid: Void? -> }
                .addOnFailureListener { e: Exception? -> }
        }
        return Result.success()
    }
}
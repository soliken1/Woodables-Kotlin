package com.intprog.woodablesapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.util.logging.Handler

class   AdminListingFragment : Fragment() {
    private var listingsLinearLayout: LinearLayout? = null
    private var db: FirebaseFirestore? = null
    private val handler = android.os.Handler()

    private var isActive = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val viewRoot = inflater.inflate(R.layout.fragment_admin_listing, container, false)
        listingsLinearLayout = viewRoot.findViewById(R.id.listingsLinearLayoutAdmin)
        db = FirebaseFirestore.getInstance()
        return viewRoot
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.postDelayed({
            loadDocumentIds() // Your data loading logic
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    private fun loadDocumentIds() {
        db!!.collectionGroup("user_jobs")
            .whereEqualTo("status", "pending")
            .orderBy(
                "jobTitle",
                Query.Direction.ASCENDING
            ) // Ensure this matches the composite index
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful && isActive) {
                    listingsLinearLayout!!.removeAllViews() // Clear existing views
                    for (document in task.result) {
                        val documentId = document.id
                        Log.d("AdminActivity", "Adding document to layout: $documentId")
                        addDocumentToLayout(documentId, document)
                    }
                } else {
                    Log.e("AdminActivity", "Error loading documents: ", task.exception)
                    Toast.makeText(context, "Error loading documents.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun approveDocument(documentId: String, document: DocumentSnapshot) {
        document.reference
            .update("status", "approved")
            .addOnSuccessListener { aVoid: Void? ->
                Toast.makeText(context, "Document approved.", Toast.LENGTH_SHORT).show()
                loadDocumentIds()
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    context,
                    "Error approving document.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteDocument(documentId: String, document: DocumentSnapshot) {
        document.reference
            .delete()
            .addOnSuccessListener { aVoid: Void? ->
                Toast.makeText(context, "Document deleted.", Toast.LENGTH_SHORT).show()
                loadDocumentIds()
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    context,
                    "Error deleting document.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addDocumentToLayout(documentId: String, document: QueryDocumentSnapshot) {
        // Inflate the layout for a single document
        val documentView = getLayoutInflater().inflate(
            R.layout.admin_listing_activity,
            listingsLinearLayout,
            false
        )

        // Set the document data
        val companyNameTextView = documentView.findViewById<TextView>(R.id.companyNameTextView)
        val jobTitleTextView = documentView.findViewById<TextView>(R.id.jobTitleTextView)
        val payRangeTextView = documentView.findViewById<TextView>(R.id.payRangeTextView)
        val detailsTextView = documentView.findViewById<TextView>(R.id.detailsTextView)
        val requirements1TextView = documentView.findViewById<TextView>(R.id.requirements1TextView)
        val requirements2TextView = documentView.findViewById<TextView>(R.id.requirements2TextView)
        val requirements3TextView = documentView.findViewById<TextView>(R.id.requirements3TextView)
        val hasBenefitsTextView = documentView.findViewById<TextView>(R.id.hasBenefitsTextView)
        val deleteButton = documentView.findViewById<Button>(R.id.deleteButton)
        val approveButton = documentView.findViewById<Button>(R.id.approveButton)
        companyNameTextView.text = "Company Name: " + document.getString("companyName")
        jobTitleTextView.text = "Job Title: " + document.getString("jobTitle")
        payRangeTextView.text = "Pay Range: " + document.getString("payRange")
        detailsTextView.text = "Details: " + document.getString("details")
        requirements1TextView.text = "Requirements 1: " + document.getString("requirements1")
        requirements2TextView.text = "Requirements 2: " + document.getString("requirements2")
        requirements3TextView.text = "Requirements 3: " + document.getString("requirements3")
        hasBenefitsTextView.text = "Has Benefits: " + document.getString("hasBenefits")

        // Set button click listeners
        deleteButton.setOnClickListener { v: View? ->
            showConfirmationDialog(
                documentId,
                documentView,
                document,
                "delete"
            )
        }
        approveButton.setOnClickListener { v: View? ->
            showConfirmationDialog(
                documentId,
                documentView,
                document,
                "approve"
            )
        }

        // Add the document view to the layout
        listingsLinearLayout!!.addView(documentView)
    }

    private fun showConfirmationDialog(
        documentId: String,
        documentView: View,
        document: DocumentSnapshot,
        action: String
    ) {
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to $action this document?")
            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                if (action == "delete") {
                    deleteDocument(documentId, document)
                } else if (action == "approve") {
                    approveDocument(documentId, document)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
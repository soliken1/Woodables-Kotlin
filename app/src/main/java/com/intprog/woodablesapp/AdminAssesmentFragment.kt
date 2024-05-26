package com.intprog.woodablesapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.concurrent.TimeUnit
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class AdminAssesmentFragment : Fragment() {

    private lateinit var assessmentLinearLayout: LinearLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_admin_assesment, container, false)

        assessmentLinearLayout = viewRoot.findViewById(R.id.assessmentLinearLayoutAdmin)

        db = FirebaseFirestore.getInstance()

        loadDocumentIds()

        return viewRoot
    }

    private fun loadDocumentIds() {
        db.collection("assessment")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val documentId = document.id
                        val fullName =
                            "${document.getString("firstName")} ${document.getString("middleName")} ${document.getString("lastName")}"
                        val expertise = document.getString("expertise")
                        val desc7 = document.getString("desc7")
                        val educ = document.getString("educ")
                        val course = document.getString("course")
                        val exp_1 = document.getString("exp_1")
                        val exp_2 = document.getString("exp_2")
                        val location = document.getString("location")
                        val email = document.getString("email")
                        val status = document.getString("status") ?: ""
                        addDocumentToLayout(
                            documentId,
                            fullName,
                            expertise,
                            desc7,
                            educ,
                            course,
                            exp_1,
                            exp_2,
                            location,
                            email,
                            status
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error loading documents.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun deleteDocument(
        documentId: String,
        documentView: View,
        email: String?,
        desc7: String?,
        fullName: String?
    ) {
        db.collection("assessment").document(documentId)
            .delete()
            .addOnSuccessListener {
                assessmentLinearLayout.removeView(documentView)
                sendDisapprovalEmail(email, desc7, fullName)
                Toast.makeText(requireContext(), "Document deleted.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error deleting document.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun approveDocument(
        documentId: String,
        documentView: View,
        email: String?,
        desc7: String?,
        fullName: String?
    ) {
        db.collection("assessment").document(documentId)
            .update("status", "approved")
            .addOnSuccessListener {
                sendApprovalEmail(email, desc7, fullName)
                disableButtons(documentView)
                scheduleDocumentDeletion(documentId)
                Toast.makeText(requireContext(), "Document approved.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error approving document.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun disableButtons(documentView: View) {
        val deleteButton: Button = documentView.findViewById(R.id.deleteButton)
        val approveButton: Button = documentView.findViewById(R.id.approveButton)
        deleteButton.isEnabled = false
        approveButton.isEnabled = false
    }

    private fun scheduleDocumentDeletion(documentId: String) {
        val data = Data.Builder()
            .putString("documentId", documentId)
            .build()

        val deleteRequest = OneTimeWorkRequest.Builder(DeleteDocumentWorker::class.java)
            .setInitialDelay(7, TimeUnit.DAYS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(deleteRequest)
    }

    private fun addDocumentToLayout(
        documentId: String,
        fullName: String,
        expertise: String?,
        desc7: String?,
        educ: String?,
        course: String?,
        exp_1: String?,
        exp_2: String?,
        location: String?,
        email: String?,
        status: String?
    ) {
        val inflater = LayoutInflater.from(requireContext())
        val itemView = inflater.inflate(R.layout.admin_item_assessment, assessmentLinearLayout, false)

        val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        val educTextView: TextView = itemView.findViewById(R.id.educTextView)
        val courseTextView: TextView = itemView.findViewById(R.id.courseTextView)
        val exp1TextView: TextView = itemView.findViewById(R.id.exp1TextView)
        val exp2TextView: TextView = itemView.findViewById(R.id.exp2TextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val approveButton: Button = itemView.findViewById(R.id.approveButton)

        fullNameTextView.text = "$fullName $expertise\n$desc7"
        educTextView.text = "Education: $educ"
        courseTextView.text = "Course: $course"
        exp1TextView.text = "Experience 1: $exp_1"
        exp2TextView.text = "Experience 2: $exp_2"
        locationTextView.text = "Location: $location"

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(documentId, itemView, email, desc7, fullName)
        }
        approveButton.setOnClickListener {
            showApproveConfirmationDialog(documentId, itemView, email, desc7, fullName)
        }

        if ("approved" == status) {
            disableButtons(itemView)
        }

        assessmentLinearLayout.addView(itemView)
    }

    private fun showDeleteConfirmationDialog(
        documentId: String,
        documentView: View,
        email: String?,
        desc7: String?,
        fullName: String?
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Disapproved Assessment")
            .setMessage("Are you sure you want to disapprove this assessment?")
            .setPositiveButton("Yes") { dialog, which ->
                deleteDocument(documentId, documentView, email, desc7, fullName)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showApproveConfirmationDialog(
        documentId: String,
        documentView: View,
        email: String?,
        desc7: String?,
        fullName: String?
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Approved Assessment")
            .setMessage("Are you sure you want to approve this assessment?")
            .setPositiveButton("Yes") { dialog, which ->
                approveDocument(documentId, documentView, email, desc7, fullName)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun sendApprovalEmail(email: String?, desc7: String?, fullName: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Woodables [Skill Assessment Approved]")
        emailIntent.putExtra(
            Intent.EXTRA_TEXT, String.format(
                "We are happy to inform you, %s, that your skill assessment has been approved. Congratulations!\n\nDate of Skill Assessment: %s\n\nPlease communicate with us to comply with the requirements needed.",
                fullName, desc7
            )
        )
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(requireContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDisapprovalEmail(email: String?, desc7: String?, fullName: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Woodables [Skill Assessment Disapproved]")
        emailIntent.putExtra(
            Intent.EXTRA_TEXT, String.format(
                "We are very sorry to inform you, %s, that your skill assessment has been disapproved.\n\nDate of Skill Assessment: %s\n\nPlease contact us for more information.",
                fullName, desc7
            )
        )
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(requireContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }
}

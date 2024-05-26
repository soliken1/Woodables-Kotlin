package com.intprog.woodablesapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateJobCardFragment : Fragment() {

    private lateinit var compName: EditText
    private lateinit var jobTitle: EditText
    private lateinit var payRange: EditText
    private lateinit var details: EditText
    private lateinit var requirements1: EditText
    private lateinit var requirements2: EditText
    private lateinit var requirements3: EditText
    private lateinit var benefitsCheckBox: CheckBox
    private lateinit var createButton: Button
    private lateinit var burgmenu: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_create_job_card, container, false)

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Reference UI elements
        compName = viewRoot.findViewById(R.id.compName)
        jobTitle = viewRoot.findViewById(R.id.jobTitle)
        payRange = viewRoot.findViewById(R.id.payRange)
        details = viewRoot.findViewById(R.id.details)
        requirements1 = viewRoot.findViewById(R.id.requirements1)
        requirements2 = viewRoot.findViewById(R.id.requirements2)
        requirements3 = viewRoot.findViewById(R.id.requirements3)
        benefitsCheckBox = viewRoot.findViewById(R.id.benefitsCheckBox)
        createButton = viewRoot.findViewById(R.id.createButton)
        burgmenu = viewRoot.findViewById(R.id.burgermenucreate)

        // Set onClickListener for the burger menu
        burgmenu.setOnClickListener {
            replaceFragment(ClientJobListFragment())
        }

        // Set onClickListener for the button
        createButton.setOnClickListener {
            saveJobListing()
        }

        return viewRoot
    }

    private fun saveJobListing() {
        val compNameStr = compName.text.toString().trim()
        val jobTitleStr = jobTitle.text.toString().trim()
        val payRangeStr = payRange.text.toString().trim()
        val detailsStr = details.text.toString().trim()
        val requirementsStr1 = requirements1.text.toString().trim()
        val requirementsStr2 = requirements2.text.toString().trim()
        val requirementsStr3 = requirements3.text.toString().trim()
        val hasBenefits = if (benefitsCheckBox.isChecked) "yes" else "no"

        if (compNameStr.isEmpty() || jobTitleStr.isEmpty() || payRangeStr.isEmpty() || detailsStr.isEmpty() || requirementsStr1.isEmpty()) {
            Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.uid

        // Create a new job listing map
        val jobListing = hashMapOf(
            "companyName" to compNameStr,
            "jobTitle" to jobTitleStr,
            "payRange" to payRangeStr,
            "details" to detailsStr,
            "requirements1" to requirementsStr1,
            "requirements2" to requirementsStr2,
            "requirements3" to requirementsStr3,
            "hasBenefits" to hasBenefits,
            "status" to "pending" // Set status to pending
        )

        // Save the job listing in the main "job_listings" collection with the user's UID as a sub-collection
        db.collection("job_listings").document(uid).collection("user_jobs")
            .add(jobListing)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(activity, "Job listing created, wait for Admin Approval", Toast.LENGTH_SHORT).show()
                // Clear input fields or navigate to another fragment/activity
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error creating job listing: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        compName.text.clear()
        jobTitle.text.clear()
        payRange.text.clear()
        details.text.clear()
        requirements1.text.clear()
        requirements2.text.clear()
        requirements3.text.clear()
        benefitsCheckBox.isChecked = false
    }

    private fun replaceFragment(frag: Fragment) {
        val fragmentManager = activity?.supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
        fragmentTransaction?.replace(R.id.contentView, frag)
        fragmentTransaction?.commit()
    }
}

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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class CreateJobCardFragment : Fragment() {
    private var compName: EditText? = null
    private var jobTitle: EditText? = null
    private var payRange: EditText? = null
    private var details: EditText? = null
    private var requirements1: EditText? = null
    private var requirements2: EditText? = null
    private var requirements3: EditText? = null
    private var benefitsCheckBox: CheckBox? = null
    private var createButton: Button? = null
    private var burgmenu: ImageView? = null
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        burgmenu?.setOnClickListener(View.OnClickListener { replaceFragment(ClientJobListFragment()) })

        // Set onClickListener for the button
        createButton?.setOnClickListener(View.OnClickListener { saveJobListing() })
        return viewRoot
    }

    private fun saveJobListing() {
        val compNameStr = compName!!.getText().toString().trim { it <= ' ' }
        val jobTitleStr = jobTitle!!.getText().toString().trim { it <= ' ' }
        val payRangeStr = payRange!!.getText().toString().trim { it <= ' ' }
        val detailsStr = details!!.getText().toString().trim { it <= ' ' }
        val requirementsStr1 = requirements1!!.getText().toString().trim { it <= ' ' }
        val requirementsStr2 = requirements2!!.getText().toString().trim { it <= ' ' }
        val requirementsStr3 = requirements3!!.getText().toString().trim { it <= ' ' }
        val hasBenefits = if (benefitsCheckBox!!.isChecked) "yes" else "no"
        if (compNameStr.isEmpty() || jobTitleStr.isEmpty() || payRangeStr.isEmpty() || detailsStr.isEmpty() || requirementsStr1.isEmpty()) {
            Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val currentUser = auth!!.currentUser
        if (currentUser == null) {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = currentUser.uid
        val userEmail = currentUser.email

        // Create a new job listing map
        val jobListing: MutableMap<String, Any?> = HashMap()
        jobListing["companyName"] = compNameStr
        jobListing["jobTitle"] = jobTitleStr
        jobListing["payRange"] = payRangeStr
        jobListing["details"] = detailsStr
        jobListing["requirements1"] = requirementsStr1
        jobListing["requirements2"] = requirementsStr2
        jobListing["requirements3"] = requirementsStr3
        jobListing["hasBenefits"] = hasBenefits
        jobListing["status"] = "pending" // Set status to pending
        jobListing["creatorEmail"] = userEmail

        // Save the job listing in the main "job_listings" collection with the user's UID as a sub-collection
        db!!.collection("job_listings").document(uid).collection("user_jobs")
            .add(jobListing)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                Toast.makeText(
                    activity,
                    "Job listing created, wait for Admin Approval",
                    Toast.LENGTH_SHORT
                ).show()
                // Clear input fields or navigate to another fragment/activity
                clearFields()
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    activity,
                    "Error creating job listing: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearFields() {
        compName!!.setText("")
        jobTitle!!.setText("")
        payRange!!.setText("")
        details!!.setText("")
        requirements1!!.setText("")
        requirements2!!.setText("")
        requirements3!!.setText("")
        benefitsCheckBox!!.setChecked(false)
    }

    private fun replaceFragment(frag: Fragment) {
        val fragmentManager = activity?.supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
        fragmentTransaction?.replace(R.id.contentView, frag)
        fragmentTransaction?.commit()
    }
}
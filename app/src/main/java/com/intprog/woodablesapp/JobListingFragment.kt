package com.intprog.woodablesapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class JobListingFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private var storageReference: StorageReference? = null
    private var userId: String? = null
    private var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_job_listing, container, false)
        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        db = FirebaseFirestore.getInstance()

        if (mAuth?.currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return rootView
        }

        userId = mAuth?.currentUser?.uid

        val profilePicture = rootView.findViewById<ImageView>(R.id.profilepicture)
        fetchProfilePicture(profilePicture)
        retrieveListings()

        return rootView
    }

    private fun fetchProfilePicture(profilePicture: ImageView) {
        storageReference?.child("profile_pictures/$userId")?.downloadUrl
            ?.addOnSuccessListener { uri: Uri? ->
                Glide.with(requireContext()).load(uri).into(profilePicture)
            }?.addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load profile picture",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun renderListing(listing: Listing?, ownerEmail: String?) {
        val inflater = LayoutInflater.from(requireContext())
        val listingView = inflater.inflate(R.layout.post_listing, null)

        val companyNameTextView = listingView.findViewById<TextView>(R.id.company_name_post)
        val jobTitleTextView = listingView.findViewById<TextView>(R.id.title_post)
        val payRangeTextView = listingView.findViewById<TextView>(R.id.pay_range_post)
        val detailsTextView = listingView.findViewById<TextView>(R.id.details_post)
        val requirements1TextView = listingView.findViewById<TextView>(R.id.requirements1_post)
        val requirements2TextView = listingView.findViewById<TextView>(R.id.requirements2_post)
        val requirements3TextView = listingView.findViewById<TextView>(R.id.requirements3_post)
        val hasBenefitsTextView = listingView.findViewById<TextView>(R.id.benefits_post)
        val applyButton = listingView.findViewById<Button>(R.id.apply_button)

        companyNameTextView.text = "Company Name/Individual Name: " + listing!!.companyName
        jobTitleTextView.text = "Job Title: " + listing.jobTitle
        payRangeTextView.text = "Pay Range: " + listing.payRange
        detailsTextView.text = "Details: " + listing.details
        requirements1TextView.text = "Requirements 1: " + listing.requirements1
        requirements2TextView.text = "Requirements 2: " + listing.requirements2
        requirements3TextView.text = "Requirements 3: " + listing.requirements3
        hasBenefitsTextView.text = "Has Benefits: " + listing.hasBenefits

        applyButton.setOnClickListener { v: View? ->
            sendEmail(listing.jobTitle, ownerEmail)
        }

        val listingContainer = requireView().findViewById<LinearLayout>(R.id.listingContainer)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(40, 40, 40, 40)
        listingView.layoutParams = layoutParams
        listingContainer.addView(listingView)
    }

    private fun sendEmail(jobTitle: String?, ownerEmail: String?) {
        val currentUserEmail = mAuth?.currentUser?.email
        val subject = "Application for $jobTitle"
        val body = """
            Dear Sir/Madam,

            I am interested in applying for the position of $jobTitle. Please find my application attached.

            Regards,
            $currentUserEmail
        """.trimIndent()
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ownerEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No email client found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveListings() {
        db?.collectionGroup("user_jobs")
            ?.whereEqualTo("status", "approved")
            ?.orderBy("jobTitle", Query.Direction.ASCENDING)
            ?.get()
            ?.addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (queryDocumentSnapshots.isEmpty) {
                    Toast.makeText(requireContext(), "No approved listings found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val listing = documentSnapshot.toObject(Listing::class.java)
                        val ownerId = documentSnapshot.reference.parent.parent!!.id
                        fetchOwnerEmail(ownerId, listing)
                    }
                }
            }
            ?.addOnFailureListener { e: Exception ->
                Log.e("JobListingFragment", "Failed to retrieve listings", e)
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve listings: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchOwnerEmail(ownerId: String, listing: Listing?) {
        db?.collection("users")?.document(ownerId)?.get()
            ?.addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val ownerEmail = documentSnapshot.getString("email")
                    renderListing(listing, ownerEmail)
                }
            }
            ?.addOnFailureListener { e: Exception ->
                Log.e("JobListingFragment", "Failed to fetch owner email", e)
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch owner email: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
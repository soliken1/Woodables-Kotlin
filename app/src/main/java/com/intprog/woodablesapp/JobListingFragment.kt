package com.intprog.woodablesapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class JobListingFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var storageReference: FirebaseStorage
    private lateinit var userId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_job_listing, container, false)

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance()
        userId = mAuth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return rootView
        }

        val profilePicture = rootView.findViewById<ImageView>(R.id.profilepicture)
        fetchProfilePicture(profilePicture)

        retrieveListings()

        return rootView
    }

    private fun fetchProfilePicture(profilePicture: ImageView) {
        storageReference.getReference("profile_pictures/$userId").downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(requireContext()).load(uri).into(profilePicture)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load profile picture", Toast.LENGTH_SHORT).show()
            }
    }

    private fun renderListing(listing: Listing) {
        // Inflate your listing item layout dynamically
        val listingView = layoutInflater.inflate(R.layout.post_listing, null) as View

        // Populate views with listing data
        listingView.findViewById<TextView>(R.id.company_name_post).text = "Company Name/Individual Name: ${listing.companyName}"
        listingView.findViewById<TextView>(R.id.title_post).text = "Job Title: ${listing.jobTitle}"
        listingView.findViewById<TextView>(R.id.pay_range_post).text = "Pay Range: ${listing.payRange}"
        listingView.findViewById<TextView>(R.id.details_post).text = "Details: ${listing.details}"
        listingView.findViewById<TextView>(R.id.requirements1_post).text = "Requirements 1: ${listing.requirements1}"
        listingView.findViewById<TextView>(R.id.requirements2_post).text = "Requirements 2: ${listing.requirements2}"
        listingView.findViewById<TextView>(R.id.requirements3_post).text = "Requirements 3: ${listing.requirements3}"
        listingView.findViewById<TextView>(R.id.benefits_post).text = "Has Benefits: ${listing.hasBenefits}"

        // Add the listing view to your LinearLayout with appropriate margins
        val listingContainer = requireView().findViewById<LinearLayout>(R.id.listingContainer)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(40, 40, 40, 40) // Add margins if needed
        listingView.layoutParams = layoutParams
        listingContainer.addView(listingView)
    }

    private fun retrieveListings() {
        val db = FirebaseFirestore.getInstance()

        db.collectionGroup("user_jobs")
            .whereEqualTo("status", "approved")
            .orderBy("jobTitle", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(context, "No approved listings found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (documentSnapshot in querySnapshot) {
                        val listing = documentSnapshot.toObject(Listing::class.java)
                        renderListing(listing)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to retrieve listings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

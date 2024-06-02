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
import android.widget.SearchView
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
    private var userFullName: String? = null // Add a variable to store the user's full name
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_job_listing, container, false)
        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference()
        db = FirebaseFirestore.getInstance()
        if (mAuth!!.currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return rootView
        }
        userId = mAuth!!.currentUser!!.uid
        val profilePicture = rootView.findViewById<ImageView>(R.id.profilepicture)
        fetchProfilePicture(profilePicture)
        retrieveUserFullName() // Fetch the user's full name
        retrieveListings()

        val searchView = rootView.findViewById<SearchView>(R.id.searchBarProfile)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchListings(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchListings(newText)
                }
                return true
            }
        })

        return rootView
    }

    private fun searchListings(query: String) {
        db!!.collectionGroup("user_jobs")
            .whereEqualTo("status", "approved")
            .orderBy("jobTitle", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                val listingContainer = view?.findViewById<LinearLayout>(R.id.listingContainer)
                listingContainer?.removeAllViews() // Clear previous results

                if (queryDocumentSnapshots.isEmpty) {
                    Toast.makeText(context, "No approved listings found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val listing = documentSnapshot.toObject(Listing::class.java)
                        if (listing.matchesQuery(query)) {
                            val ownerEmail = documentSnapshot.getString("creatorEmail")
                            renderListing(listing, ownerEmail)
                        }
                    }
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("JobListingFragment", "Failed to retrieve listings", e)
                Toast.makeText(context, "Failed to retrieve listings: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    // Add a method in the Listing class to check if the listing matches the query
    private fun Listing.matchesQuery(query: String): Boolean {
        return companyName?.contains(query, ignoreCase = true) == true ||
                jobTitle?.contains(query, ignoreCase = true) == true ||
                payRange?.contains(query, ignoreCase = true) == true ||
                details?.contains(query, ignoreCase = true) == true ||
                requirements1?.contains(query, ignoreCase = true) == true ||
                requirements2?.contains(query, ignoreCase = true) == true ||
                requirements3?.contains(query, ignoreCase = true) == true
    }

    private fun fetchProfilePicture(profilePicture: ImageView) {
        storageReference!!.child("profile_pictures/$userId").getDownloadUrl()
            .addOnSuccessListener { uri: Uri? ->
                Glide.with(requireContext()).load(uri).into(profilePicture)
            }.addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    context,
                    "Failed to load profile picture",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun retrieveUserFullName() {
        db!!.collection("users").document(userId!!).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val firstName = documentSnapshot.getString("First Name")
                    val middleName = documentSnapshot.getString("Middle Name")
                    val lastName = documentSnapshot.getString("Last Name")

                    // Concatenate the fields to form the full name
                    userFullName = String.format("%s %s %s", firstName, middleName, lastName)
                        .trim { it <= ' ' }

                    // Log or use the full name as needed
                    Log.d("JobListingFragment", "User Full Name: $userFullName")
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("JobListingFragment", "Failed to fetch user full name", e)
                Toast.makeText(
                    context,
                    "Failed to fetch user full name: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun renderListing(listing: Listing?, ownerEmail: String?) {
        val inflater = LayoutInflater.from(context)
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
            sendEmail(
                listing.jobTitle, ownerEmail
            )
        }
        val listingContainer = requireView().findViewById<LinearLayout>(R.id.listingContainer)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(40, 40, 40, 40)
        listingView.setLayoutParams(layoutParams)
        listingContainer.addView(listingView)
    }

    private fun sendEmail(jobTitle: String?, ownerEmail: String?) {
        val subject = "Application for $jobTitle"
        val body =
            "Dear Sir/Madam,\n\nI am interested in applying for the position of $jobTitle. Can you fill me in with the details and requirements.\n\nBest regards,\n$userFullName"
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto:")) // Only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ownerEmail))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
        }
    }

    //    public void retrieveListings() {
    //        db.collectionGroup("user_jobs")
    //                .whereEqualTo("status", "approved")
    //                .orderBy("jobTitle", Query.Direction.ASCENDING)
    //                .get()
    //                .addOnSuccessListener(queryDocumentSnapshots -> {
    //                    if (queryDocumentSnapshots.isEmpty()) {
    //                        Toast.makeText(getContext(), "No approved listings found.", Toast.LENGTH_SHORT).show();
    //                    } else {
    //                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
    //                            Listing listing = documentSnapshot.toObject(Listing.class);
    //                            String ownerId = documentSnapshot.getReference().getParent().getParent().getId();
    //                            fetchOwnerEmail(ownerId, listing);
    //                        }
    //                    }
    //                })
    //                .addOnFailureListener(e -> {
    //                    Log.e("JobListingFragment", "Failed to retrieve listings", e);
    //                    Toast.makeText(getContext(), "Failed to retrieve listings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    //                });
    //    }
    fun retrieveListings() {
        db!!.collectionGroup("user_jobs")
            .whereEqualTo("status", "approved")
            .orderBy("jobTitle", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (queryDocumentSnapshots.isEmpty) {
                    Toast.makeText(context, "No approved listings found.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val listing = documentSnapshot.toObject(Listing::class.java)
                        // Fetch the creator's email directly from the listing document
                        val ownerEmail = documentSnapshot.getString("creatorEmail")
                        renderListing(listing, ownerEmail)
                    }
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("JobListingFragment", "Failed to retrieve listings", e)
                Toast.makeText(
                    context,
                    "Failed to retrieve listings: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchOwnerEmail(ownerId: String, listing: Listing) {
        db!!.collection("users").document(ownerId).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val ownerEmail = documentSnapshot.getString("email")
                    renderListing(listing, ownerEmail)
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("JobListingFragment", "Failed to fetch owner email", e)
                Toast.makeText(
                    context,
                    "Failed to fetch owner email: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}
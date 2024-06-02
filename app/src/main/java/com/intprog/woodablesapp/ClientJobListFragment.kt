package com.intprog.woodablesapp

import android.os.Bundle
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

class ClientJobListFragment : Fragment() {
    private lateinit var joblistingsLinearLayout: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var searchView: SearchView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_client_job_list, container, false)

        joblistingsLinearLayout = viewRoot.findViewById(R.id.joblistingsLinearLayout)
        val bkbutton: ImageView = viewRoot.findViewById(R.id.backbutton)
        searchView = viewRoot.findViewById(R.id.searchBarProfile)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        userId = mAuth.currentUser?.uid.orEmpty()

        loadUserJobList("")

        bkbutton.setOnClickListener {
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val createjobfrag = CreateJobCardFragment()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
            fragmentTransaction.replace(R.id.contentView, createjobfrag)
            fragmentTransaction.commit()
        }


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                loadUserJobList(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loadUserJobList(newText.orEmpty())
                return true
            }
        })

        return viewRoot
    }

    private fun loadUserJobList(searchQuery: String) {
        db.collection("job_listings")
            .document(userId)
            .collection("user_jobs")
            .whereEqualTo("status", "approved")
            .orderBy("jobTitle", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    joblistingsLinearLayout.removeAllViews()
                    for (document in task.result!!) {
                        val listing = document.toObject(Listing::class.java)
                        if (listingMatchesSearchQuery(listing, searchQuery)) {
                            addListingToLayout(document.id, listing)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error loading job listings: ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun listingMatchesSearchQuery(listing: Listing, searchQuery: String): Boolean {
        return listing.companyName?.contains(searchQuery, ignoreCase = true) ?: false ||
                listing.jobTitle?.contains(searchQuery, ignoreCase = true) ?: false ||
                listing.details?.contains(searchQuery, ignoreCase = true) ?: false ||
                listing.payRange?.contains(searchQuery, ignoreCase = true) ?: false ||
                listing.requirements1?.contains(searchQuery, ignoreCase = true) ?: false ||
                listing.requirements2?.contains(searchQuery, ignoreCase = true) ?: false ||
                listing.requirements3?.contains(searchQuery, ignoreCase = true) ?: false
    }

    private fun addListingToLayout(documentId: String, listing: Listing) {
        val listingView =
            layoutInflater.inflate(R.layout.listing_item, joblistingsLinearLayout, false)

        val companyNameTextView: TextView = listingView.findViewById(R.id.companyNameTextView)
        val jobTitleTextView: TextView = listingView.findViewById(R.id.jobTitleTextView)
        val payRangeTextView: TextView = listingView.findViewById(R.id.payRangeTextView)
        val detailsTextView: TextView = listingView.findViewById(R.id.detailsTextView)
        val requirements1TextView: TextView = listingView.findViewById(R.id.requirements1TextView)
        val requirements2TextView: TextView = listingView.findViewById(R.id.requirements2TextView)
        val requirements3TextView: TextView = listingView.findViewById(R.id.requirements3TextView)
        val hasBenefitsTextView: TextView = listingView.findViewById(R.id.hasBenefitsTextView)
        val deleteButton: Button = listingView.findViewById(R.id.deleteButton)

        companyNameTextView.text = "Company Name: ${listing.companyName}"
        jobTitleTextView.text = "Job Title: ${listing.jobTitle}"
        payRangeTextView.text = "Pay Range: ${listing.payRange}"
        detailsTextView.text = "Details: ${listing.details}"
        requirements1TextView.text = "Requirements 1: ${listing.requirements1}"
        requirements2TextView.text = "Requirements 2: ${listing.requirements2}"
        requirements3TextView.text = "Requirements 3: ${listing.requirements3}"
        hasBenefitsTextView.text = "Has Benefits: ${listing.hasBenefits}"

        deleteButton.setOnClickListener {
            deleteJobListing(documentId)
        }

        joblistingsLinearLayout.addView(listingView)
    }

    private fun deleteJobListing(documentId: String) {
        db.collection("job_listings")
            .document(userId)
            .collection("user_jobs")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Job listing deleted", Toast.LENGTH_SHORT).show()
                loadUserJobList("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting job listing: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

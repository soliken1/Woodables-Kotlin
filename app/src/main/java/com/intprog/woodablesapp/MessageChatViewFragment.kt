package com.intprog.woodablesapp

import android.content.Intent
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MessageChatViewFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var userId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val viewRoot = inflater.inflate(R.layout.fragment_message_chat_view, container, false)

        val linearLayoutUsers = viewRoot.findViewById<LinearLayout>(R.id.linear_layout_users)

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        userId = mAuth.currentUser!!.uid

        val profilePicture = viewRoot.findViewById<ImageView>(R.id.profilepicture)
        fetchProfilePicture(profilePicture)

        // Retrieve user data from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnSuccessListener { documents ->
            for (document in documents) {
                // Get user data fields from Firestore document
                val firstName = document.getString("First Name")
                val lastName = document.getString("Last Name")

                // Inflate the user layout XML
                val userView = inflater.inflate(R.layout.message_user, null)

                // Set user data to the user layout
                val textViewName = userView.findViewById<TextView>(R.id.username_message)
                textViewName.text = "$firstName $lastName"

                // Set OnClickListener to redirect to ChatPersonActivity
                userView.setOnClickListener {
                    // Get the user ID from the Firestore document
                    val userID = document.id

                    // Start ChatPersonActivity and pass the selected user's ID
                    val intent = Intent(context, ChatPersonActivity::class.java)
                    intent.putExtra("userID", userID)
                    intent.putExtra("name", firstName)
                    startActivity(intent)
                }

                // Add the user layout to the LinearLayout
                linearLayoutUsers.addView(userView)
            }
        }.addOnFailureListener { e ->
            // Handle any errors
        }

        return viewRoot
    }

    private fun fetchProfilePicture(profilePicture: ImageView) {
        storageReference.child("profile_pictures/$userId").downloadUrl.addOnSuccessListener { uri ->
            Glide.with(requireContext()).load(uri).into(profilePicture)
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to load profile picture", Toast.LENGTH_SHORT).show()
        }
    }
}

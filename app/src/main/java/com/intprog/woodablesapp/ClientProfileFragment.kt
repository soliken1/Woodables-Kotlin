package com.intprog.woodablesapp

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientProfileFragment : Fragment() {

    private lateinit var profileName: TextView
    private lateinit var woodworkerRole: TextView
    private lateinit var profileDesc2: TextView
    private lateinit var profileDesc3: TextView
    private lateinit var profileDesc4: TextView
    private lateinit var profileDesc5: TextView
    private lateinit var profileDesc6: TextView
    private lateinit var profileDesc7: TextView
    private lateinit var profilePicture: ImageView
    private lateinit var logoutBtn: Button
    private lateinit var storageReference: FirebaseStorage
    private lateinit var userId: String
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_client_profile, container, false)

        profileName = viewRoot.findViewById(R.id.profileName)
        woodworkerRole = viewRoot.findViewById(R.id.profileCategory)
        profileDesc2 = viewRoot.findViewById(R.id.profileDesc2)
        profileDesc3 = viewRoot.findViewById(R.id.profileDesc3)
        profileDesc4 = viewRoot.findViewById(R.id.profileDesc4)
        profileDesc5 = viewRoot.findViewById(R.id.profileDesc5)
        profileDesc6 = viewRoot.findViewById(R.id.profileDesc6)
        profileDesc7 = viewRoot.findViewById(R.id.profileDesc7)
        profilePicture = viewRoot.findViewById(R.id.profilepicture)
        val editProfile: Button = viewRoot.findViewById(R.id.editProfile)


        editProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileClientActivity::class.java)
            startActivityForResult(intent, 1)
        }

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance()
        userId = mAuth.currentUser!!.uid

        logoutBtn = viewRoot.findViewById(R.id.logout)

        logoutBtn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }


        val intent = activity?.intent
        var fullName = intent?.getStringExtra("FullName")
        var role = intent?.getStringExtra("ROLE")

        if (fullName == null || role == null) {
            val preferences = activity?.getSharedPreferences("user_info", Context.MODE_PRIVATE)
            fullName = preferences?.getString("fullname", "Default Name")
            role = preferences?.getString("role", "Default Role")

            // Log retrieved values
            Log.d("Preferences", "Full Name: $fullName, Role: $role")
        }


        profileName.text = fullName
        woodworkerRole.text = role

        // Fetch profile descriptions from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("profile_descriptions").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val profileDescriptions = documentSnapshot.toObject(ProfileDescriptions::class.java)
                    profileDesc2.text = "Address: " + profileDescriptions?.desc2
                    profileDesc3.text = "Phone Number: " +profileDescriptions?.desc3
                    profileDesc4.text = "Facebook: " +profileDescriptions?.desc4
                    profileDesc5.text = "Email: " +profileDescriptions?.desc5
                    profileDesc6.text = "Profile Link: " +profileDescriptions?.desc6
                    profileDesc7.text = "Joined: " +getAccountCreationDate()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load descriptions", Toast.LENGTH_SHORT).show()
            }

        // Fetch profile picture from Firebase Storage
        storageReference.reference.child("profile_pictures/$userId").downloadUrl.addOnSuccessListener { uri ->
            Glide.with(requireContext()).load(uri).into(profilePicture)
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to load profile picture", Toast.LENGTH_SHORT).show()
        }

        // Fetch profile picture from Firebase Storage using ProfilePictureManager
        ProfilePictureManager.fetchProfilePicture(requireContext(), profilePicture)

        return viewRoot
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val fullName = data.getStringExtra("FULL_NAME")
            val desc2 = data.getStringExtra("DESC2")
            val desc3 = data.getStringExtra("DESC3")
            val desc4 = data.getStringExtra("DESC4")
            val desc5 = data.getStringExtra("DESC5")
            val desc6 = data.getStringExtra("DESC6")
            val desc7 = data.getStringExtra("DESC7")

            fullName?.let {
                profileName.text = it
                val preferences = requireActivity().getSharedPreferences("user_info", AppCompatActivity.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("fullname", it)
                editor.apply()
            }

            profileDesc2.text = desc2
            profileDesc3.text = desc3
            profileDesc4.text = desc4
            profileDesc5.text = desc5
            profileDesc6.text = desc6
            profileDesc7.text = desc7

            storageReference.reference.child("profile_pictures/$userId").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(requireContext()).load(uri).into(profilePicture)
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load updated profile picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to get the account creation date
    private fun getAccountCreationDate(): String {
        val creationTimestamp = mAuth.currentUser?.metadata?.creationTimestamp
        val creationDate = Date(creationTimestamp!!)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(creationDate)
    }


    private fun replaceFragment(frag: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contentView, frag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}

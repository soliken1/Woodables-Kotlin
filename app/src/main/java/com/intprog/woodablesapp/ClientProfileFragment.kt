package com.intprog.woodablesapp

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

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
    private lateinit var logoutBtn: ImageView
    private lateinit var storageReference: StorageReference
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
        storageReference = FirebaseStorage.getInstance().reference
        userId = mAuth.currentUser!!.uid

        logoutBtn = viewRoot.findViewById(R.id.logout)
        logoutBtn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

        val fullName = requireActivity().intent.getStringExtra("FullName")
        val role = requireActivity().intent.getStringExtra("ROLE")
        if (fullName == null || role == null) {
            val preferences = requireActivity().getSharedPreferences("user_info", AppCompatActivity.MODE_PRIVATE)
            val defaultName = "Default Name"
            val defaultRole = "Default Role"
            profileName.text = preferences.getString("fullname", defaultName)
            woodworkerRole.text = preferences.getString("role", defaultRole)
        } else {
            profileName.text = fullName
            woodworkerRole.text = role
        }

        storageReference.child("profile_pictures/$userId").downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(requireContext()).load(uri).into(profilePicture)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load profile picture", Toast.LENGTH_SHORT).show()
            }

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

            storageReference.child("profile_pictures/$userId").downloadUrl
                .addOnSuccessListener { uri ->
                    Glide.with(requireContext()).load(uri).into(profilePicture)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load updated profile picture", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createPopUpWindow(clientName: String, layout: View) {
        val inflater = LayoutInflater.from(requireActivity())
        val popup = inflater.inflate(R.layout.activity_client_profile_followed_dummy, null)

        val clientFollowed: TextView = popup.findViewById(R.id.followingName)
        clientFollowed.text = "You've Followed $clientName"
        val confirm: Button = popup.findViewById(R.id.confirmbtn)

        val focusable = true
        val followedPopUp = PopupWindow(popup, 800, 500, focusable)
        layout.post {
            followedPopUp.showAtLocation(layout, Gravity.CENTER, 0, 0)
        }

        confirm.setOnClickListener {
            followedPopUp.dismiss()
        }
    }

}

package com.intprog.woodablesapp

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

object ProfilePictureManager {
    private const val PREFS_NAME = "user_info"
    private const val PROFILE_PICTURE_KEY = "profile_picture_url"
    @JvmStatic
    fun fetchProfilePicture(context: Context, imageView: ImageView?) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val profilePictureUrl = preferences.getString(PROFILE_PICTURE_KEY, null)
        if (profilePictureUrl != null) {
            Glide.with(context).load(profilePictureUrl).into(imageView!!)
        } else {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_pictures/$userId")
            storageReference.getDownloadUrl().addOnSuccessListener { uri: Uri ->
                val editor = preferences.edit()
                editor.putString(PROFILE_PICTURE_KEY, uri.toString())
                editor.apply()
                Glide.with(context).load(uri).into(imageView!!)
            }.addOnFailureListener { e: Exception? -> }
        }
    }

    @JvmStatic
    fun updateProfilePicture(context: Context, uri: Uri) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(PROFILE_PICTURE_KEY, uri.toString())
        editor.apply()
    }
}
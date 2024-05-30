package com.intprog.woodablesapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var backBtn: ImageView
    private lateinit var profilePicture: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var userId: String
    private var profileUpdated = false
    private var descriptionsUpdated = false
    private var pictureUpdated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        userId = mAuth.currentUser!!.uid

        val saveButton: Button = findViewById(R.id.saveEdit)
        val firstNameEditText: EditText = findViewById(R.id.editFirstName)
        val middleNameEditText: EditText = findViewById(R.id.editMiddleName)
        val lastNameEditText: EditText = findViewById(R.id.editLastName)

        val desc2EditText: EditText = findViewById(R.id.profileDesc2)
        val desc3EditText: EditText = findViewById(R.id.profileDesc3)
        val desc4EditText: EditText = findViewById(R.id.profileDesc4)
        val desc5EditText: EditText = findViewById(R.id.profileDesc5)
        val desc6EditText: EditText = findViewById(R.id.profileDesc6)
        val desc7EditText: EditText = findViewById(R.id.profileDesc7)
        profilePicture = findViewById(R.id.profilepicture)
        val pictureEdit: ImageView = findViewById(R.id.camerlogo)
        backBtn = findViewById(R.id.backbutton)

        backBtn.setOnClickListener { finish() }

        // Fetch and populate user data
        db.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val firstName = documentSnapshot.getString("First Name")
                val middleName = documentSnapshot.getString("Middle Name")
                val lastName = documentSnapshot.getString("Last Name")
                val role = documentSnapshot.getString("Role")

                if (role == "client") {
                    val companyName = documentSnapshot.getString("Company Name")
                } else {
                }
                firstNameEditText.setText(firstName)
                middleNameEditText.setText(middleName)
                lastNameEditText.setText(lastName)
            }
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
        }

        // Fetch and populate profile descriptions
        db.collection("profile_descriptions").document(userId).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                desc2EditText.setText(documentSnapshot.getString("desc2"))
                desc3EditText.setText(documentSnapshot.getString("desc3"))
                desc4EditText.setText(documentSnapshot.getString("desc4"))
                desc5EditText.setText(documentSnapshot.getString("desc5"))
                desc6EditText.setText(documentSnapshot.getString("desc6"))
                val creationDate = FirebaseAuth.getInstance().currentUser!!.metadata?.creationTimestamp.toString()
                desc7EditText.setText(getFormattedDate(creationDate.toLong()))
                desc7EditText.isEnabled = false // Disable editing
            }
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileActivity, "Error loading descriptions", Toast.LENGTH_SHORT).show()
        }

        // Fetch and populate profile picture
        storageReference.child("profile_pictures/$userId").downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this@EditProfileActivity).load(uri).into(profilePicture)
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileActivity, "Error loading profile picture", Toast.LENGTH_SHORT).show()
        }

        pictureEdit.setOnClickListener { openFileChooser() }

        saveButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val middleName = middleNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val desc2 = desc2EditText.text.toString()
            val desc3 = desc3EditText.text.toString()
            val desc4 = desc4EditText.text.toString()
            val desc5 = desc5EditText.text.toString()
            val desc6 = desc6EditText.text.toString()
            val desc7 = desc7EditText.text.toString()

            // Check if any of the description fields are not empty
            val hasNonEmptyDescriptions = !desc2.isEmpty() || !desc3.isEmpty() || !desc4.isEmpty() ||
                    !desc5.isEmpty() || !desc6.isEmpty() || !desc7.isEmpty()

            // Update user profile
            if (!firstName.isEmpty() || !middleName.isEmpty() || !lastName.isEmpty()) {
                val userData: MutableMap<String, Any> = HashMap()
                userData["First Name"] = firstName
                userData["Middle Name"] = middleName
                userData["Last Name"] = lastName

                db.collection("users").document(userId)
                    .update(userData)
                    .addOnSuccessListener {
                        profileUpdated = true
                        showToastUpdate()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@EditProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
            } else {
                profileUpdated = true
                showToastUpdate()
            }

            // Update descriptions if there are non-empty values
            if (hasNonEmptyDescriptions) {
                val profileDescriptions = ProfileDescriptions(desc2, desc3, desc4, desc5, desc6, desc7)
                db.collection("profile_descriptions").document(userId)
                    .set(profileDescriptions)
                    .addOnSuccessListener {
                        descriptionsUpdated = true
                        showToastUpdate()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@EditProfileActivity, "Error updating descriptions", Toast.LENGTH_SHORT).show()
                    }
            } else {
                descriptionsUpdated = true
                showToastUpdate()
            }

            val fullName = "$firstName $middleName $lastName"
            val intent = Intent()
            intent.putExtra("FULL_NAME", fullName)
            intent.putExtra("DESC2", desc2)
            intent.putExtra("DESC3", desc3)
            intent.putExtra("DESC4", desc4)
            intent.putExtra("DESC5", desc5)
            intent.putExtra("DESC6", desc6)
            intent.putExtra("DESC7", desc7)
            setResult(Activity.RESULT_OK, intent)
        }
    }


    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                profilePicture.setImageBitmap(bitmap)
                uploadImageToFirebase(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val profilePicRef = storageReference.child("profile_pictures/$userId")
        profilePicRef.putBytes(data).addOnSuccessListener { taskSnapshot ->
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                ProfilePictureManager.updateProfilePicture(this@EditProfileActivity, uri)
                Toast.makeText(this@EditProfileActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                Glide.with(this@EditProfileActivity).load(uri).into(profilePicture)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this@EditProfileActivity, "Error uploading profile picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToastUpdate() {
        if (profileUpdated && descriptionsUpdated && pictureUpdated) {
            Toast.makeText(this@EditProfileActivity, "Profile, descriptions, and picture updated", Toast.LENGTH_SHORT).show()
            finish()
        } else if (profileUpdated && descriptionsUpdated) {
            Toast.makeText(this@EditProfileActivity, "Profile and descriptions updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getFormattedDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

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

class EditProfileClientActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var userId: String
    private var profileUpdated = false
    private var descriptionsUpdated = false
    private var pictureUpdated = false

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                try {
                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    profilePicture.setImageBitmap(bitmap)
                    uploadImageToFirebase(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_client)

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

        val backBtn: ImageView = findViewById(R.id.backbutton)
        backBtn.setOnClickListener { finish() }

        db.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val firstName: String? = documentSnapshot.getString("First Name")
                val middleName: String? = documentSnapshot.getString("Middle Name")
                val lastName: String? = documentSnapshot.getString("Last Name")
                firstNameEditText.setText(firstName)
                middleNameEditText.setText(middleName)
                lastNameEditText.setText(lastName)
            }
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileClientActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
        }

        db.collection("profile_descriptions").document(userId).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                desc2EditText.setText(documentSnapshot.getString("desc2"))
                desc3EditText.setText(documentSnapshot.getString("desc3"))
                desc4EditText.setText(documentSnapshot.getString("desc4"))
                desc5EditText.setText(documentSnapshot.getString("desc5"))
                desc6EditText.setText(documentSnapshot.getString("desc6"))
                val creationDate = mAuth.currentUser!!.metadata?.creationTimestamp
                desc7EditText.setText(creationDate?.let { getFormattedDate(it) })
                desc7EditText.isEnabled = false // Disable editing
            }
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileClientActivity, "Error loading descriptions", Toast.LENGTH_SHORT).show()
        }

        storageReference.child("profile_pictures/$userId").downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this@EditProfileClientActivity).load(uri).into(profilePicture)
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileClientActivity, "Error loading profile picture", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.camerlogo).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        saveButton.setOnClickListener {
            val firstName: String = firstNameEditText.text.toString()
            val middleName: String = middleNameEditText.text.toString()
            val lastName: String = lastNameEditText.text.toString()
            val desc2: String = desc2EditText.text.toString()
            val desc3: String = desc3EditText.text.toString()
            val desc4: String = desc4EditText.text.toString()
            val desc5: String = desc5EditText.text.toString()
            val desc6: String = desc6EditText.text.toString()
            val desc7: String = desc7EditText.text.toString()

            val hasNonEmptyDescriptions: Boolean =
                !desc2.isEmpty() || !desc3.isEmpty() || !desc4.isEmpty() || !desc5.isEmpty() || !desc6.isEmpty() || !desc7.isEmpty()

            if (!firstName.isEmpty() || !middleName.isEmpty() || !lastName.isEmpty()) {
                val userData = hashMapOf(
                    "First Name" to firstName,
                    "Middle Name" to middleName,
                    "Last Name" to lastName
                )

                db.collection("users").document(userId)
                    .update(userData as Map<String, Any>)
                    .addOnSuccessListener {
                        profileUpdated = true
                        showToastUpdate()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@EditProfileClientActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
            } else {
                profileUpdated = true
                showToastUpdate()
            }

            if (hasNonEmptyDescriptions) {
                val profileDescriptions = ProfileDescriptions(desc2, desc3, desc4, desc5, desc6, desc7)
                db.collection("profile_descriptions").document(userId)
                    .set(profileDescriptions)
                    .addOnSuccessListener {
                        descriptionsUpdated = true
                        showToastUpdate()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@EditProfileClientActivity, "Error updating descriptions", Toast.LENGTH_SHORT).show()
                    }
            } else {
                descriptionsUpdated = true
                showToastUpdate()
            }

            val fullName = "$firstName $middleName $lastName"
            val intent = Intent().apply {
                putExtra("FULL_NAME", fullName)
                putExtra("DESC2", desc2)
                putExtra("DESC3", desc3)
                putExtra("DESC4", desc4)
                putExtra("DESC5", desc5)
                putExtra("DESC6", desc6)
                putExtra("DESC7", desc7)
            }
            setResult(Activity.RESULT_OK, intent)
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data: ByteArray = baos.toByteArray()

        val profilePicRef: StorageReference = storageReference.child("profile_pictures/$userId")
        profilePicRef.putBytes(data).addOnSuccessListener { taskSnapshot ->
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                ProfilePictureManager.updateProfilePicture(this@EditProfileClientActivity, uri)
                Toast.makeText(this@EditProfileClientActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                Glide.with(this@EditProfileClientActivity).load(uri).into(profilePicture)
            }
        }.addOnFailureListener {
            Toast.makeText(this@EditProfileClientActivity, "Error uploading profile picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToastUpdate() {
        if (profileUpdated && descriptionsUpdated && pictureUpdated) {
            Toast.makeText(this@EditProfileClientActivity, "Profile, descriptions, and picture updated", Toast.LENGTH_SHORT).show()
            finish()
        } else if (profileUpdated && descriptionsUpdated) {
            Toast.makeText(this@EditProfileClientActivity, "Profile and descriptions updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getFormattedDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

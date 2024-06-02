package com.intprog.woodablesapp

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class CreatePostActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private lateinit var title: EditText
    private lateinit var content: EditText
    private lateinit var clickPost: Button
    private lateinit var closeview: ImageView
    private lateinit var toCam: ImageView
    private lateinit var toGal: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_post)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        title = findViewById(R.id.titletxt)
        content = findViewById(R.id.contenttxt)
        clickPost = findViewById(R.id.postbtn)
        closeview = findViewById(R.id.close)

        clickPost.setOnClickListener {
            val postTitle = title.text.toString()
            val postContent = content.text.toString()
            val preferences = getSharedPreferences("user_info", MODE_PRIVATE)
            val name = "w/" + preferences?.getString("fullname", "Default Name")
            val role = preferences?.getString("role", "Default Role")
            val status = "pending"

            // Log the contents of preferences
            val userInfo = "Name: $name, Role: $role"
            Log.d("UserInfo", userInfo)

            // Check if title and content are not empty
            if (!postTitle.isEmpty() && !postContent.isEmpty()) {
                // Call the uploadPost method to store the post in Firestore
                uploadPost(postTitle, postContent, name, status)

                // Clear EditText fields
                title.text.clear()
                content.text.clear()
            } else {
                // Show a Snackbar or toast message indicating that fields are empty
                Snackbar.make(it, "Title and content cannot be empty", Snackbar.LENGTH_SHORT).show()
            }
        }


        closeview.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
        }

    }

    private fun uploadPost(title: String, message: String, userName: String, status: String) {
        val db = FirebaseFirestore.getInstance()
        val post = Post(title, message, userName, status)

        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                // Post added successfully
                Log.d("Firestore", "Post added successfully with ID: ${documentReference.id}")
                // Show a toast indicating successful posting
                Toast.makeText(this, "Please Wait For Admin Approval On Your Post", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("Firestore", "Error adding post: ${e.message}", e)
            }
    }
}

package com.intprog.woodablesapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText
    private lateinit var toProfile: Button
    private lateinit var toForgot: TextView
    private lateinit var toRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        emailEditText = findViewById(R.id.email)
        passEditText = findViewById(R.id.password)
        toProfile = findViewById(R.id.toprofilelogin)
        toForgot = findViewById(R.id.forgotpassword)
        toRegister = findViewById(R.id.register)

        toProfile.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passEditText.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                passEditText.error = "Password is required"
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = mAuth.currentUser

                        if ("admin@admin.com" == email || user?.isEmailVerified == true) {
                            // Fetch user information from Firestore
                            db.collection("users").document(user?.uid ?: "").get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        val firstname = documentSnapshot.getString("First Name")
                                        val lastname = documentSnapshot.getString("Last Name")
                                        val middlename = documentSnapshot.getString("Middle Name")
                                        val role = documentSnapshot.getString("Role")
                                        Log.d("LoginActivity", "First Name: $firstname, Middle Name: $middlename, Role: $role")

                                        if ("admin" == role) {
                                            val adminActivityIntent = Intent(this@LoginActivity, AdminMainScreen::class.java)
                                            startActivity(adminActivityIntent)
                                        } else {
                                            val toUserProfile = Intent(this@LoginActivity, MainScreenActivity::class.java)
                                            val fullName = "$firstname $middlename $lastname"
                                            toUserProfile.putExtra("ROLE", role) // Add the role to the intent
                                            toUserProfile.putExtra("FullName", fullName)
                                            val preferences: SharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE)
                                            val editor = preferences.edit()
                                            editor.putString("fullname", fullName)
                                            editor.putString("role", role)
                                            editor.apply()
                                            startActivity(toUserProfile)
                                        }

                                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_LONG).show()
                                    } else {
                                        Log.d("LoginActivity", "User document does not exist")
                                        Toast.makeText(this@LoginActivity, "User document does not exist", Toast.LENGTH_LONG).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LoginActivity", "Error fetching user information", e)
                                    Toast.makeText(this@LoginActivity, "Error fetching user information", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(this@LoginActivity, "Please verify your email address first.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // If the task fails, check the exception to determine the cause
                        val errorMessage = task.exception?.message
                        errorMessage?.let {
                            when {
                                it.contains("password") -> {
                                    // Password is incorrect
                                    Toast.makeText(this@LoginActivity, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show()
                                }
                                it.contains("email") -> {
                                    // Email does not exist
                                    Toast.makeText(this@LoginActivity, "Email does not exist. Please check your email or register.", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    // Other authentication errors
                                    Toast.makeText(this@LoginActivity, "Authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }
                        } ?: run {
                            // Other unexpected errors
                            Toast.makeText(this@LoginActivity, "Authentication failed. Please check your credentials.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

        toForgot.setOnClickListener {
            val navforgot = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(navforgot)
        }

        toRegister.setOnClickListener {
            val navreg = Intent(this@LoginActivity, UserInfoActivity::class.java)
            startActivity(navreg)
        }
    }
}

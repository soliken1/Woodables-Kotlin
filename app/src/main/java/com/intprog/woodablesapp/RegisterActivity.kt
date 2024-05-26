package com.intprog.woodablesapp

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var logclick: TextView
    private lateinit var registerbtn: Button
    private lateinit var passText: EditText
    private lateinit var emailText: EditText
    private lateinit var tologin: Intent
    private lateinit var progressDialog: ProgressDialog

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    private lateinit var db: FirebaseFirestore

    private val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        logclick = findViewById(R.id.loginhere)
        registerbtn = findViewById(R.id.regclick)
        passText = findViewById(R.id.password)
        emailText = findViewById(R.id.email)
        progressDialog = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser!!
        db = FirebaseFirestore.getInstance()

        logclick.setOnClickListener {
            tologin = Intent(this, LoginActivity::class.java)
            startActivity(tologin)
        }

        registerbtn.setOnClickListener {
            performAuth()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) {
            return false
        }
        val specialChars = "!@#$%^&*()-_=+\\|[{]};:'\",<.>/?"
        var hasSpecialChar = false
        var hasUppercase = false
        var hasLowercase = false
        var hasNumber = false

        for (c in password) {
            if (specialChars.contains(c)) {
                hasSpecialChar = true
            } else if (c.isUpperCase()) {
                hasUppercase = true
            } else if (c.isLowerCase()) {
                hasLowercase = true
            } else if (c.isDigit()) {
                hasNumber = true
            }
        }
        return hasSpecialChar && hasUppercase && hasLowercase && hasNumber
    }

    private fun performAuth() {
        val password = passText.text.toString()
        val email = emailText.text.toString()

        if (!email.matches(emailPattern)) {
            emailText.error = "Enter Correct Email"
        } else if (!isPasswordValid(password)) {
            passText.error = "Password must contain at least 8 characters, including at least one uppercase letter, one or more lowercase letters, one or more numbers, and one special character"
        } else {
            progressDialog.setTitle("Registration")
            progressDialog.setMessage("Registering for a while, Please wait")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result!!.signInMethods!!.isNotEmpty()) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                        emailText.text.clear()
                        passText.text.clear()
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { createUserTask ->
                            if (createUserTask.isSuccessful) {
                                sendEmailVerification()
                            } else {
                                progressDialog.dismiss()
                                Toast.makeText(this, createUserTask.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to check email registration", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendEmailVerification() {
        val user = mAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserData()
                Toast.makeText(this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData() {
        val user = mAuth.currentUser
        if (user != null) {
            val userID = user.uid

            val intent = intent
            val lName = intent.getStringExtra("LName")
            val fName = intent.getStringExtra("FName")
            val mName = intent.getStringExtra("MName")
            val cName = intent.getStringExtra("CName")
            val role = intent.getStringExtra("Role")

            val userData = hashMapOf<String, Any?>(
                "Email" to user.email,
                "Role" to role,
                "First Name" to fName,
                "Last Name" to lName,
                "Middle Name" to mName
            )

            if ("client" == role) {
                userData["Company Name"] = cName
            }

            db.collection("users").document(userID)
                .set(userData)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Registration Successful. Please verify your email.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

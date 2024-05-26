package com.intprog.woodablesapp

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {

    private lateinit var forgotEmail: EditText
    private lateinit var recoverBtn: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        mAuth = FirebaseAuth.getInstance()

        forgotEmail = findViewById(R.id.forgotemail)
        recoverBtn = findViewById(R.id.recoverbtn)

        recoverBtn.setOnClickListener {
            val email = forgotEmail.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                forgotEmail.error = "Email is required"
                return@setOnClickListener
            }

            // Send password reset email
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@ForgotPasswordActivity, "Password reset email sent. Please check your email.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Failed to send password reset email. Please check your email and try again.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}

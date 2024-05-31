package com.intprog.woodablesapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AssessmentActivity : AppCompatActivity() {

    private lateinit var lNameIn: EditText
    private lateinit var fNameIn: EditText
    private lateinit var mNameIn: EditText
    private lateinit var doaIn: EditText
    private lateinit var expertiseIn: EditText
    private lateinit var sendBtn: Button
    private lateinit var backBtn: ImageView

    // Firestore instance
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    // SharedPreferences instance
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assessment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        lNameIn = findViewById(R.id.assesslName)
        fNameIn = findViewById(R.id.assessfName)
        mNameIn = findViewById(R.id.assessmName)
        doaIn = findViewById(R.id.assesDOA)
        expertiseIn = findViewById(R.id.assessExpertise)
        sendBtn = findViewById(R.id.bookBtn)
        backBtn = findViewById(R.id.backbutton)

        // Fetch and set user data
        fetchAndSetUserData()

        sendBtn.setOnClickListener { saveUserData() }
        backBtn.setOnClickListener { finish() }
    }

    // Method to fetch user data from Firestore and set in EditText fields
    private fun fetchAndSetUserData() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val firstName = documentSnapshot.getString("First Name") ?: ""
                        val middleName = documentSnapshot.getString("Middle Name") ?: ""
                        val lastName = documentSnapshot.getString("Last Name") ?: ""
                        val email = documentSnapshot.getString("Email") ?: ""

                        fNameIn.setText(firstName)
                        mNameIn.setText(middleName)
                        lNameIn.setText(lastName)

                        // Make the fields non-editable
                        fNameIn.isEnabled = false
                        mNameIn.isEnabled = false
                        lNameIn.isEnabled = false

                        // Save the email in SharedPreferences for later use
                        sharedPreferences.edit().putString("userEmail", email).apply()
                    } else {
                        Toast.makeText(this@AssessmentActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@AssessmentActivity, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@AssessmentActivity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to save user data in Firestore
    private fun saveUserData() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val email = sharedPreferences.getString("userEmail", "") ?: ""

            // Validate that all required fields are filled
            val lastName = lNameIn.text.toString().trim()
            val firstName = fNameIn.text.toString().trim()
            val middleName = mNameIn.text.toString().trim()
            val dateOfAssessment = doaIn.text.toString().trim()
            val expertise = expertiseIn.text.toString().trim()

            if (lastName.isEmpty() || firstName.isEmpty() || middleName.isEmpty() || dateOfAssessment.isEmpty() || expertise.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return
            }

            fetchProfileDescriptions(uid) { desc2, desc3, desc4, desc5, desc6, desc7 ->
                val assessmentData = hashMapOf(
                    "lastName" to lastName,
                    "firstName" to firstName,
                    "middleName" to middleName,
                    "dateOfAssessment" to dateOfAssessment,
                    "expertise" to expertise,
                    "email" to email,
                    // Add profile descriptions to assessment data
                    "exp_1" to desc2,
                    "exp_2" to desc3,
                    "educ" to desc4,
                    "course" to desc5,
                    "location" to desc6,
                    "desc7" to desc7
                )

                // Use set with the document ID as the user's UID
                db.collection("assessment").document(uid).set(assessmentData)
                    .addOnSuccessListener {
                        Toast.makeText(this@AssessmentActivity, "You've successfully applied for Skills Assessment. We'll contact you soon!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@AssessmentActivity, "Cannot apply for Skills Assessment. Try again later", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this@AssessmentActivity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    // Method to fetch profile descriptions from Firestore
    private fun fetchProfileDescriptions(uid: String, callback: (String?, String?, String?, String?, String?, String?) -> Unit) {
        db.collection("profile_descriptions").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val desc2 = documentSnapshot.getString("desc2")
                    val desc3 = documentSnapshot.getString("desc3")
                    val desc4 = documentSnapshot.getString("desc4")
                    val desc5 = documentSnapshot.getString("desc5")
                    val desc6 = documentSnapshot.getString("desc6")
                    val desc7 = documentSnapshot.getString("desc7")

                    callback(desc2, desc3, desc4, desc5, desc6, desc7)
                } else {
                    callback(null, null, null, null, null, null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@AssessmentActivity, "Error fetching profile descriptions", Toast.LENGTH_SHORT).show()
                callback(null, null, null, null, null, null)
            }
    }
}

package com.intprog.woodablesapp

import com.google.firebase.firestore.DocumentId

class Assessment  // Empty constructor needed for Firestore
{
    // Getters and setters for all fields
    @DocumentId
    val id: String? = null
    @JvmField
    val course: String? = null
    val dateOfAssessment: String? = null
    val desc7: String? = null
    val educ: String? = null
    val exp_1: String? = null
    val exp_2: String? = null
    val expertise: String? = null
    @JvmField
    val firstName: String? = null
    @JvmField
    val lastName: String? = null
    val location: String? = null
    val middleName: String? = null
}
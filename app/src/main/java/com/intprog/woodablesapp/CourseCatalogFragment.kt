package com.intprog.woodablesapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class CourseCatalogFragment : Fragment() {

    private lateinit var toSkillAssess: Button
    private lateinit var backBtn: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var courseListContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_course_catalog, container, false)

        courseListContainer = viewRoot.findViewById(R.id.course_list_container)
        db = FirebaseFirestore.getInstance()
        toSkillAssess = viewRoot.findViewById(R.id.skillassess)
        backBtn = viewRoot.findViewById(R.id.backbutton)

        toSkillAssess.setOnClickListener {
            val toAssessment = Intent(viewRoot.context, AssessmentActivity::class.java)
            startActivity(toAssessment)
        }

        backBtn.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val learnFragment = LearnCourseFragment()
            if (fragmentManager != null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
                fragmentTransaction.replace(R.id.contentView, learnFragment)
                fragmentTransaction.commit()
            }
        }

        loadCourses()
        return viewRoot
    }

    private fun loadCourses() {
        db.collection("course")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null) {
                        for (document in querySnapshot) {
                            val title = document.getString("title")
                            val description = document.getString("description")
                            val details = document.getString("details")
                            val link = document.getString("link")
                            addCourseToUI(title, description, details, link)
                        }
                    }
                } else {
                    // Handle the error
                }
            }
    }

    private fun addCourseToUI(title: String?, description: String?, details: String?, link: String?) {
        val courseItem = LayoutInflater.from(context).inflate(R.layout.item_course, courseListContainer, false)

        val courseTitle = courseItem.findViewById<TextView>(R.id.course_title)
        val courseDescription = courseItem.findViewById<TextView>(R.id.course_description)

        courseTitle.text = title
        courseDescription.text = description

        courseItem.setOnClickListener {
            showCourseDetailsDialog(title, description, details, link)
        }

        courseListContainer.addView(courseItem)
    }

    private fun showCourseDetailsDialog(title: String?, description: String?, details: String?, link: String?) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_course_details, null)
        builder.setView(dialogView)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_course_title)
        val dialogDetails = dialogView.findViewById<TextView>(R.id.dialog_course_details)
        val dialogLink = dialogView.findViewById<TextView>(R.id.dialog_course_link)
        val buttonCancel = dialogView.findViewById<Button>(R.id.button_cancel)
        val buttonEnroll = dialogView.findViewById<Button>(R.id.button_enroll)

        dialogTitle.text = title
        dialogDetails.text = details
        dialogLink.text = link

        val dialog = builder.create()

        buttonCancel.setOnClickListener { dialog.dismiss() }

        buttonEnroll.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid

                val courseData = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "details" to details,
                    "link" to link
                )

                db.collection("enrolled_courses").document(userId)
                    .collection("courses")
                    .add(courseData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Enrolled successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}

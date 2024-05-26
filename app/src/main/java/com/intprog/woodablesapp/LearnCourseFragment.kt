package com.intprog.woodablesapp

import android.app.Activity
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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot


class LearnCourseFragment : Fragment() {

    private val ASSESSMENT_REQUEST_CODE = 1
    private lateinit var toAssess: Button
    private lateinit var toCatalog: Button
    private lateinit var refreshButton: Button
    private lateinit var enrolledCoursesContainer: LinearLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_learn_course, container, false)

        toAssess = viewRoot.findViewById(R.id.skillassess)
        toCatalog = viewRoot.findViewById(R.id.browsecoursecatalog)
        refreshButton = viewRoot.findViewById(R.id.refreshbutton)
        enrolledCoursesContainer = viewRoot.findViewById(R.id.enrolled_courses_container)
        db = FirebaseFirestore.getInstance()

        toCatalog.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val catalogFragment = CourseCatalogFragment()
            fragmentManager?.beginTransaction()?.replace(R.id.contentView, catalogFragment)?.commit()
        }

        toAssess.setOnClickListener {
            val toAssessIntent = Intent(context, AssessmentActivity::class.java)
            startActivityForResult(toAssessIntent, ASSESSMENT_REQUEST_CODE)
        }

        refreshButton.setOnClickListener { refreshFragment() }

        loadEnrolledCourses()

        return viewRoot
    }


    private fun refreshFragment() {
        val fragmentManager = activity?.supportFragmentManager
        fragmentManager?.beginTransaction()?.replace(R.id.contentView, LearnCourseFragment())?.commit()
    }

    private fun loadEnrolledCourses() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            db.collection("enrolled_courses").document(userId).collection("courses")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val querySnapshot = task.result
                        querySnapshot?.let {
                            enrolledCoursesContainer.removeAllViews() // Clear existing views
                            for (document in querySnapshot) {
                                val title = document.getString("title") ?: ""
                                val description = document.getString("description") ?: ""
                                val details = document.getString("details") ?: ""
                                addEnrolledCourseToUI(title, description, details)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to load courses.", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(context, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addEnrolledCourseToUI(title: String, description: String, details: String) {
        val courseItem = LayoutInflater.from(context).inflate(R.layout.item_enrolled_course, enrolledCoursesContainer, false)

        courseItem.findViewById<TextView>(R.id.course_title).text = title
        courseItem.findViewById<TextView>(R.id.course_description).text = description

        courseItem.setOnClickListener { showCourseDetailsDialog(title, description, details) }

        enrolledCoursesContainer.addView(courseItem)
    }

    private fun showCourseDetailsDialog(title: String, description: String, details: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_learncourse_details, null)

        val dialogTitleTextView = dialogView.findViewById<TextView>(R.id.dialog_course_title)
        val dialogDetailsTextView = dialogView.findViewById<TextView>(R.id.dialog_course_details)
        val cancelButton = dialogView.findViewById<ImageView>(R.id.button_cancel)
        val dropButton = dialogView.findViewById<Button>(R.id.button_drop)
        val launchButton = dialogView.findViewById<Button>(R.id.button_launch)

        dialogTitleTextView.text = title
        dialogDetailsTextView.text = details

        val dialog = builder.setView(dialogView).create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        dropButton.setOnClickListener {
            dropCourse(title)
            dialog.dismiss()
        }

        launchButton.setOnClickListener {
            launchCourse(title)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun dropCourse(title: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            db.collection("enrolled_courses").document(userId).collection("courses")
                .whereEqualTo("title", title)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isEmpty) {
                        for (document in task.result) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Course dropped successfully.", Toast.LENGTH_SHORT).show()
                                    loadEnrolledCourses() // Reload the courses to update UI
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to drop course.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(context, "Course not found.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun launchCourse(title: String) {
        Toast.makeText(context, "Launching course: $title", Toast.LENGTH_SHORT).show()
        // Implement your launch course logic here
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ASSESSMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Handle the result from the assessment
        }
    }
}

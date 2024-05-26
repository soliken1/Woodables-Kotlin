package com.intprog.woodablesapp

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LearnCourseFragment : Fragment() {

    private lateinit var toAssess: Button
    private lateinit var toCatalog: Button
    private lateinit var refreshButton: Button
    private lateinit var enrolledCoursesContainer: LinearLayout
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val ASSESSMENT_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_learn_course, container, false)

        toAssess = viewRoot.findViewById(R.id.skillassess)
        toCatalog = viewRoot.findViewById(R.id.browsecoursecatalog)
        refreshButton = viewRoot.findViewById(R.id.refreshbutton)
        enrolledCoursesContainer = viewRoot.findViewById(R.id.enrolled_courses_container)
        db = FirebaseFirestore.getInstance()

        toCatalog.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contentView, CourseCatalogFragment())
                .commit()
        }

        toAssess.setOnClickListener {
            val toAssess = Intent(requireContext(), AssessmentActivity::class.java)
            startActivityForResult(toAssess, ASSESSMENT_REQUEST_CODE)
        }

        refreshButton.setOnClickListener { loadEnrolledCourses() }

        loadEnrolledCourses()

        return viewRoot
    }

    private fun loadEnrolledCourses() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            db.collection("enrolled_courses").document(userId).collection("courses")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        enrolledCoursesContainer.removeAllViews() // Clear existing views
                        task.result?.forEach { document ->
                            val title = document.getString("title")
                            val description = document.getString("description")
                            val details = document.getString("details")
                            title?.let { addEnrolledCourseToUI(it, description, details) }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load courses.", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(requireContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addEnrolledCourseToUI(title: String, description: String?, details: String?) {
        val courseItem = layoutInflater.inflate(R.layout.item_enrolled_course, enrolledCoursesContainer, false)

        val courseTitle: TextView = courseItem.findViewById(R.id.course_title)
        val courseDescription: TextView = courseItem.findViewById(R.id.course_description)

        courseTitle.text = title
        courseDescription.text = description

        courseItem.setOnClickListener { showCourseDetailsDialog(title, description, details) }

        enrolledCoursesContainer.addView(courseItem)
    }

    private fun showCourseDetailsDialog(title: String, description: String?, details: String?) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_course_details, null)
        builder.setView(dialogView)

        val dialogTitle: TextView = dialogView.findViewById(R.id.dialog_course_title)
        val dialogDetails: TextView = dialogView.findViewById(R.id.dialog_course_details)
        val buttonCancel: Button = dialogView.findViewById(R.id.button_cancel)

        dialogTitle.text = title
        dialogDetails.text = details

        val dialog = builder.create()

        buttonCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ASSESSMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Handle the result from the assessment
        }
    }
}

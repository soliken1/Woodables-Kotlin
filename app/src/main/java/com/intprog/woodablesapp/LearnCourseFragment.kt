package com.intprog.woodablesapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LearnCourseFragment : Fragment() {

    private val ASSESSMENT_REQUEST_CODE = 1
    private lateinit var toAssess: Button
    private lateinit var toCatalog: Button
    private lateinit var refreshButton: Button
    private lateinit var enrolledCoursesContainer: LinearLayout
    private lateinit var searchBar: SearchView
    private lateinit var db: FirebaseFirestore
    private var allEnrolledCourses: List<DocumentSnapshot> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_learn_course, container, false)

        toAssess = viewRoot.findViewById(R.id.skillassess)
        toCatalog = viewRoot.findViewById(R.id.browsecoursecatalog)
        refreshButton = viewRoot.findViewById(R.id.refreshbutton)
        enrolledCoursesContainer = viewRoot.findViewById(R.id.enrolled_courses_container)
        searchBar = viewRoot.findViewById(R.id.searchBarProfile)
        db = FirebaseFirestore.getInstance()

        toCatalog.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val catalogFragment = CourseCatalogFragment()
            if (fragmentManager != null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.contentView, catalogFragment)
                fragmentTransaction.commit()
            }
        }

        toAssess.setOnClickListener {
            val toAssess = Intent(viewRoot.context, AssessmentActivity::class.java)
            startActivityForResult(toAssess, ASSESSMENT_REQUEST_CODE)
        }

        refreshButton.setOnClickListener {
            refreshFragment()
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterEnrolledCourses(newText)
                return true
            }
        })

        loadEnrolledCourses()

        return viewRoot
    }

    private fun refreshFragment() {
        val fragmentManager = activity?.supportFragmentManager
        if (fragmentManager != null) {
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.contentView, LearnCourseFragment())
            fragmentTransaction.commit()
        }
    }

    private fun loadEnrolledCourses() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("enrolled_courses").document(userId).collection("courses")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val querySnapshot = task.result
                        if (querySnapshot != null) {
                            allEnrolledCourses = querySnapshot.documents
                            displayEnrolledCourses(allEnrolledCourses)
                        }
                    } else {
                        Toast.makeText(context, "Failed to load courses.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayEnrolledCourses(courses: List<DocumentSnapshot>) {
        enrolledCoursesContainer.removeAllViews() // Clear existing views
        for (document in courses) {
            val title = document.getString("title")
            val description = document.getString("description")
            val details = document.getString("details")
            val link = document.getString("link")
            addEnrolledCourseToUI(title, description, details, link)
        }
    }

    private fun addEnrolledCourseToUI(title: String?, description: String?, details: String?, link: String?) {
        val courseItem = LayoutInflater.from(context).inflate(R.layout.item_enrolled_course, enrolledCoursesContainer, false)

        val courseTitle = courseItem.findViewById<TextView>(R.id.course_title)
        val courseDescription = courseItem.findViewById<TextView>(R.id.course_description)

        courseTitle.text = title
        courseDescription.text = description

        courseItem.setOnClickListener {
            showCourseDetailsDialog(title, description, details, link)
        }

        enrolledCoursesContainer.addView(courseItem)
    }

    private fun showCourseDetailsDialog(title: String?, description: String?, details: String?, link: String?) {
        context?.let { context ->
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_learncourse_details, null)
            builder.setView(dialogView)

            val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_course_title)
            val dialogDetails = dialogView.findViewById<TextView>(R.id.dialog_course_details)
            val dialogLink = dialogView.findViewById<TextView>(R.id.dialog_course_link)
            val buttonCancel = dialogView.findViewById<ImageView>(R.id.button_cancel)
            val buttonDropCourse = dialogView.findViewById<Button>(R.id.button_drop)
            val buttonLaunchCourse = dialogView.findViewById<Button>(R.id.button_launch)

            dialogTitle.text = title
            dialogDetails.text = details

            if (link != null) {
                dialogLink.text = link
                dialogLink.visibility = View.VISIBLE
                buttonLaunchCourse.isEnabled = true
            } else {
                dialogLink.visibility = View.GONE
                buttonLaunchCourse.isEnabled = false
            }

            val dialog = builder.create()

            buttonCancel.setOnClickListener { dialog.dismiss() }

            buttonDropCourse.setOnClickListener {
                dropCourse(title)
                dialog.dismiss()
            }

            buttonLaunchCourse.setOnClickListener {
                if (link != null) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(browserIntent)
                } else {
                    Toast.makeText(context, "No link available for this course.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun dropCourse(title: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
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
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to drop course.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(context, "Course not found.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun filterEnrolledCourses(query: String?) {
        val filteredCourses = if (query.isNullOrEmpty()) {
            allEnrolledCourses
        } else {
            allEnrolledCourses.filter { document ->
                val title = document.getString("title")?.contains(query, true) ?: false
                val description = document.getString("description")?.contains(query, true) ?: false
                val details = document.getString("details")?.contains(query, true) ?: false
                title || description || details
            }
        }
        displayEnrolledCourses(filteredCourses)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ASSESSMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Handle the result from the assessment
        }
    }
}
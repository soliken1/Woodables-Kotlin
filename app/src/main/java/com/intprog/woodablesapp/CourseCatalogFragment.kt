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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.SearchView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class CourseCatalogFragment : Fragment() {

    private lateinit var toSkillAssess: Button
    private lateinit var backBtn: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var courseListContainer: LinearLayout
    private lateinit var searchBar: SearchView
    private var allCourses: List<DocumentSnapshot> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_course_catalog, container, false)

        courseListContainer = viewRoot.findViewById(R.id.course_list_container)
        db = FirebaseFirestore.getInstance()
        toSkillAssess = viewRoot.findViewById(R.id.skillassess)
        backBtn = viewRoot.findViewById(R.id.backbutton)
        searchBar = viewRoot.findViewById(R.id.searchBarProfile)

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

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCourses(newText)
                return true
            }
        })

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
                        allCourses = querySnapshot.documents
                        displayCourses(allCourses)
                    }

                } else {
                    Toast.makeText(context, "Failed to load courses.", Toast.LENGTH_SHORT).show()
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
            showTermsAndConditionsDialog(title, description, details, link, dialog)
        }

        dialog.show()
    }

    private fun showTermsAndConditionsDialog(title: String?, description: String?, details: String?, link: String?, parentDialog: AlertDialog) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val termsView = inflater.inflate(R.layout.dialog_terms_conditions, null)
        builder.setView(termsView)

        val buttonAccept = termsView.findViewById<Button>(R.id.button_accept)
        val buttonDecline = termsView.findViewById<Button>(R.id.button_decline)

        val termsDialog = builder.create()

        buttonAccept.setOnClickListener {
            enrollInCourse(title, description, details, link)
            termsDialog.dismiss()
            parentDialog.dismiss()
        }

        buttonDecline.setOnClickListener {
            termsDialog.dismiss()
        }

        termsDialog.show()
    }

    private fun enrollInCourse(title: String?, description: String?, details: String?, link: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("enrolled_courses").document(userId)
                .collection("courses")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
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
                                Toast.makeText(context, "Course enrolled successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "You are already enrolled in this course.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to check enrollment status. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayCourses(courses: List<DocumentSnapshot>) {
        courseListContainer.removeAllViews()
        for (document in courses) {
            val title = document.getString("title")
            val description = document.getString("description")
            val details = document.getString("details")
            val link = document.getString("link")
            addCourseToUI(title, description, details, link)
        }
    }

    private fun filterCourses(query: String?) {
        val filteredCourses = if (query.isNullOrEmpty()) {
            allCourses
        } else {
            allCourses.filter { document ->
                val title = document.getString("title")?.contains(query, true) ?: false
                val description = document.getString("description")?.contains(query, true) ?: false
                val details = document.getString("details")?.contains(query, true) ?: false
                title || description || details
            }
        }
        displayCourses(filteredCourses)
    }
}
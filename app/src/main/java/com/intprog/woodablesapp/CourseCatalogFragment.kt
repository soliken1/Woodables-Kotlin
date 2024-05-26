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
import com.google.firebase.firestore.QuerySnapshot

class CourseCatalogFragment : Fragment() {

    private lateinit var toSkillAssess: Button
    private lateinit var backBtn: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var courseListContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val learnFragment = LearnCourseFragment()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
            fragmentTransaction.replace(R.id.contentView, learnFragment)
            fragmentTransaction.commit()
        }

        loadCourses()
        return viewRoot
    }

    private fun loadCourses() {
        db.collection("course")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot: QuerySnapshot? = task.result
                    querySnapshot?.let {
                        for (document: QueryDocumentSnapshot in it) {
                            val title = document.getString("title")
                            val description = document.getString("description")
                            val details = document.getString("details")
                            addCourseToUI(title, description, details)
                        }
                    }
                } else {
                    // Handle the error
                }
            }
    }

    private fun addCourseToUI(title: String?, description: String?, details: String?) {
        val courseItem: View = LayoutInflater.from(context).inflate(R.layout.item_course, courseListContainer, false)

        val courseTitle: TextView = courseItem.findViewById(R.id.course_title)
        val courseDescription: TextView = courseItem.findViewById(R.id.course_description)

        courseTitle.text = title
        courseDescription.text = description

        courseItem.setOnClickListener { showCourseDetailsDialog(title, description, details) }

        courseListContainer.addView(courseItem)
    }

    private fun showCourseDetailsDialog(title: String?, description: String?, details: String?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater: LayoutInflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_course_details, null)
        builder.setView(dialogView)

        val dialogTitle: TextView = dialogView.findViewById(R.id.dialog_course_title)
        val dialogDetails: TextView = dialogView.findViewById(R.id.dialog_course_details)
        val buttonCancel: Button = dialogView.findViewById(R.id.button_cancel)
        val buttonEnroll: Button = dialogView.findViewById(R.id.button_enroll)

        dialogTitle.text = title
        dialogDetails.text = details

        val dialog: AlertDialog = builder.create()

        buttonCancel.setOnClickListener { dialog.dismiss() }

        buttonEnroll.setOnClickListener {
            val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId: String = currentUser.uid

                val courseData: MutableMap<String, Any> = HashMap()
                courseData["title"] = title ?: ""
                courseData["description"] = description ?: ""
                courseData["details"] = details ?: ""

                db.collection("enrolled_courses").document(userId)
                    .collection("courses")
                    .add(courseData)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(requireContext(), "Enrolled successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}

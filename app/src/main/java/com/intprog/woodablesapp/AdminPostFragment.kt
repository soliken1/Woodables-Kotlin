package com.intprog.woodablesapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AdminPostFragment : Fragment() {
    private var postsLinearLayout: LinearLayout? = null
    private var db: FirebaseFirestore? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val viewRoot = inflater.inflate(R.layout.fragment_admin_post, container, false)
        postsLinearLayout = viewRoot.findViewById(R.id.postsLinearLayoutAdmin)
        db = FirebaseFirestore.getInstance()
        loadPosts()
        return viewRoot
    }

    private fun loadPosts() {
        db!!.collection("posts")
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val documentId = document.id
                        val title = document.getString("title")
                        val message = document.getString("message")
                        val userName = document.getString("userName")
                        val status = document.getString("status") // Fetch status

                        Log.d("AdminPostActivity", "Adding document to layout: $documentId")
                        addPostToLayout(documentId, title, message, userName, status)
                    }
                } else {
                    Log.e("AdminPostActivity", "Error loading documents: ", task.exception)
                    Toast.makeText(context, "Error loading documents.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deletePost(documentId: String, postView: View) {
        db!!.collection("posts").document(documentId)
            .delete()
            .addOnSuccessListener { aVoid: Void? ->
                postsLinearLayout!!.removeView(postView)
                Toast.makeText(context, "Post deleted.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    context,
                    "Error deleting post.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun approvePost(documentId: String, postView: View) {
        db!!.collection("posts").document(documentId)
            .update("status", "approved")
            .addOnSuccessListener { aVoid: Void? ->
                Toast.makeText(context, "Post approved.", Toast.LENGTH_SHORT).show()
                // Hide the approve button after successful approval
                val approveButton = postView.findViewById<Button>(R.id.approveButton)
                if (approveButton != null) {
                    approveButton.visibility = View.GONE
                }
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    context,
                    "Error approving post.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addPostToLayout(
        documentId: String,
        title: String?,
        message: String?,
        userName: String?,
        status: String?
    ) {
        val postView = getLayoutInflater().inflate(R.layout.admin_post_item, postsLinearLayout, false)
        val titleTextView = postView.findViewById<TextView>(R.id.titleTextView)
        val messageTextView = postView.findViewById<TextView>(R.id.messageTextView)
        val userNameTextView = postView.findViewById<TextView>(R.id.userNameTextView)
        val deleteButton = postView.findViewById<Button>(R.id.deleteButton)
        val approveButton = postView.findViewById<Button>(R.id.approveButton)
        titleTextView.text = title
        messageTextView.text = message
        userNameTextView.text = userName

        // Conditionally show/hide the approve button based on the post status
        if (status == "pending") {
            approveButton.visibility = View.VISIBLE
        } else {
            approveButton.visibility = View.GONE
        }

        deleteButton.setOnClickListener { v: View? ->
            showConfirmationDialog(
                documentId,
                postView,
                "delete"
            )
        }
        approveButton.setOnClickListener { v: View? ->
            showConfirmationDialog(
                documentId,
                postView,
                "approve"
            )
        }
        postsLinearLayout!!.addView(postView)
    }

    private fun showConfirmationDialog(documentId: String, postView: View, action: String) {
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to $action this post?")
            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                if (action == "delete") {
                    deletePost(documentId, postView)
                } else if (action == "approve") {
                    approvePost(documentId, postView)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
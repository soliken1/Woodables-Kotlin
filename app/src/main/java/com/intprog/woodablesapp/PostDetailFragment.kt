package com.intprog.woodablesapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.content.SharedPreferences
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*


class PostDetailFragment : Fragment() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var postId: String
    private lateinit var commentContainer: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: String
    private lateinit var backBtn: ImageView
    private var keyboardVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment's layout instead of post_item.xml
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve post details from arguments
        val postTitle = arguments?.getString("postTitle")
        val postMessage = arguments?.getString("postMessage")
        val postUser = arguments?.getString("postUser")
        postId = arguments?.getString("postId") ?: ""

        backBtn = view.findViewById(R.id.backbutton)

        backBtn.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val comFragment = CommunityFragment()
            if (fragmentManager != null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
                fragmentTransaction.replace(R.id.contentView, comFragment)
                fragmentTransaction.commit()
            }
        }

        // Populate views with post details
        view.findViewById<TextView>(R.id.title).text = postTitle
        view.findViewById<TextView>(R.id.text).text = postMessage
        view.findViewById<TextView>(R.id.postusername).text = postUser

        // Initialize views
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.send_button)
        commentContainer = view.findViewById(R.id.comment_container)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Get current user's username
        val preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE)
        currentUser = "w/" + preferences.getString("fullname", "Default Name")

        sendButton.setOnClickListener {
            // Get the comment text
            val commentText = messageInput.text.toString().trim()

            // Check if comment is not empty
            if (commentText.isNotEmpty()) {
                // Save the comment to Firestore
                saveComment(commentText)
            }
        }

        // Listen for changes in comments
        listenForComments()
        // Adjust layout based on keyboard visibility
        // Adjust layout based on keyboard visibility
        val mainView = view.findViewById<View>(R.id.main)
        val messageArea = view.findViewById<LinearLayout>(R.id.messagearea)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            if (imeInsets.bottom > 0) {
                // Keyboard is open
                keyboardVisible = true
                adjustMessageAreaPadding(messageArea, imeInsets.bottom)
            } else {
                // Keyboard is closed
                if (keyboardVisible) {
                    keyboardVisible = false
                    adjustMessageAreaPadding(messageArea, 0)
                }
            }
            insets
        }
    }

    private fun adjustMessageAreaPadding(messageArea: LinearLayout, bottomPadding: Int) {
        messageArea.setPadding(0, 0, 0, bottomPadding)
    }

    private fun saveComment(commentText: String) {
        // Create a new comment document
        val commentData = hashMapOf(
            "username" to currentUser,
            "comment" to commentText,
            "postId" to postId, // Reference to the post
            "date" to Calendar.getInstance().time // Current date
        )

        // Add the comment document to the "comments" collection
        db.collection("comments")
            .add(commentData)
            .addOnSuccessListener { documentReference ->
                // Get the newly added comment from Firestore
                db.collection("comments").document(documentReference.id)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        // Convert the Firestore document to a Comment object
                        val comment = documentSnapshot.toObject(Comment::class.java)
                        // Render the new comment immediately
                        comment?.let { renderComment(it) }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure to fetch the newly added comment
                    }

                // Clear the input field after successful comment submission
                messageInput.setText("")
                // You can also display a success message or update the UI if needed
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    private fun listenForComments() {
        // Listen for real-time updates in the "comments" collection
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("date", Query.Direction.ASCENDING) // Order comments by date in descending order
            .addSnapshotListener { value, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                // Iterate through each added comment document
                for (doc in value!!.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {
                        // Get comment data
                        val comment = doc.document.toObject(Comment::class.java)
                        // Render the comment
                        renderComment(comment)
                    }
                }
            }
    }

    private fun renderComment(comment: Comment) {
        // Inflate the comment layout
        val inflater = LayoutInflater.from(context)
        val commentView = inflater.inflate(R.layout.comment_item, null)

        // Populate views with comment data
        val usernameTextView: TextView = commentView.findViewById(R.id.comment_username)
        val commentTextView: TextView = commentView.findViewById(R.id.comment_text)

        usernameTextView.text = comment.username
        commentTextView.text = comment.comment

        // Add spacing between comment views
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.comment_spacing))
        commentView.layoutParams = layoutParams

        // Add the comment view to the comment container
        commentContainer.addView(commentView)
    }
}

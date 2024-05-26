package com.intprog.woodablesapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CommunityFragment : Fragment() {

    private lateinit var addPost: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_community, container, false)

        addPost = viewRoot.findViewById(R.id.addpost)

        addPost.setOnClickListener {
            val toPost = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(toPost)
        }

        // Call retrievePosts to fetch and render posts from Firestore
        retrievePosts()

        return viewRoot
    }

    private fun retrievePosts() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereNotEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    val post = documentSnapshot.toObject(Post::class.java)
                    renderPost(post)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun renderPost(post: Post) {
        // Inflate your post layout dynamically
        val inflater = LayoutInflater.from(context)
        val postView = inflater.inflate(R.layout.post_item, null)

        // Populate views with post data
        val titleTextView: TextView = postView.findViewById(R.id.title)
        val messageTextView: TextView = postView.findViewById(R.id.text)
        val userNameTextView: TextView = postView.findViewById(R.id.postusername)

        titleTextView.text = post.title
        messageTextView.text = post.message
        userNameTextView.text = post.userName

        // Add the post view to your ScrollView
        val postContainer: LinearLayout? = view?.findViewById(R.id.postContainer)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(40, 40, 40, 40)
        postView.layoutParams = layoutParams
        postContainer?.addView(postView)
    }
}

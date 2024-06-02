package com.intprog.woodablesapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CommunityFragment : Fragment() {

    private lateinit var addPost: ImageView
    private lateinit var searchView: SearchView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_community, container, false)

        addPost = viewRoot.findViewById(R.id.addpost)
        searchView = viewRoot.findViewById(R.id.searchBarProfile)

        addPost.setOnClickListener {
            val toPost = Intent(viewRoot.context, CreatePostActivity::class.java)
            startActivity(toPost)
        }

        retrievePosts()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchPosts(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchPosts(newText)
                }
                return true
            }
        })

        return viewRoot
    }

    private fun retrievePosts() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereNotEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    val postId = documentSnapshot.id // Retrieve the document ID
                    val post = documentSnapshot.toObject(Post::class.java)
                    renderPost(postId, post) // Pass the document ID to renderPost method
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun searchPosts(query: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereNotEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val filteredPosts = queryDocumentSnapshots.filter { documentSnapshot ->
                    val post = documentSnapshot.toObject(Post::class.java)
                    post.title?.contains(query, ignoreCase = true) == true ||
                            post.message?.contains(query, ignoreCase = true) == true ||
                            post.userName?.contains(query, ignoreCase = true) == true
                }
                val postContainer: LinearLayout? = view?.findViewById(R.id.postContainer)
                postContainer?.removeAllViews()
                for (documentSnapshot in filteredPosts) {
                    val postId = documentSnapshot.id
                    val post = documentSnapshot.toObject(Post::class.java)
                    renderPost(postId, post)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun renderPost(postId: String, post: Post) {
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

        // Set OnClickListener to redirect to the post detail fragment
        postView.setOnClickListener {
            // Start PostDetailFragment and pass the post details as arguments
            val fragment = PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("postId", postId)
                    putString("postTitle", post.title)
                    putString("postMessage", post.message)
                    putString("postUser", post.userName)
                }
            }
            // Replace the current fragment with PostDetailFragment
            fragmentManager?.beginTransaction()?.replace(R.id.fragment_container, fragment)?.commit()
        }

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
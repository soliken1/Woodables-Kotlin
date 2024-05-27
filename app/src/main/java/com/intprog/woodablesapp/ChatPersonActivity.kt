package com.intprog.woodablesapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot


class ChatPersonActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var selectedUserID: String
    private lateinit var selectedUsername: String
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var chatLayout: LinearLayout
    private lateinit var bkbtn: ImageView
    private lateinit var messagearea: LinearLayout

    private var messageListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_person)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Get the selected user's ID from the intent extras
        selectedUserID = intent.getStringExtra("userID") ?: ""
        selectedUsername = intent.getStringExtra("name") ?: ""

        findViewById<TextView>(R.id.receiverUser).text = selectedUsername

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        chatLayout = findViewById(R.id.chat_layout)
        bkbtn = findViewById(R.id.backbutton)

        bkbtn.setOnClickListener { finish() }

        messageInput.requestFocus()

        messagearea = findViewById(R.id.messagearea)

        // Set OnClickListener to the send button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Load messages for the selected user
        loadMessages(selectedUserID)
    }

    override fun onStart() {
        super.onStart()
        // Start listening for real-time updates
        startListeningForMessages()
    }

    override fun onStop() {
        super.onStop()
        // Stop listening for real-time updates
        stopListeningForMessages()
    }

    private fun startListeningForMessages() {
        // Set up a listener for new messages
        messageListener = db.collection("messages")
            .whereIn("sender", listOf(FirebaseAuth.getInstance().currentUser?.uid, selectedUserID))
            .whereIn("receiver", listOf(FirebaseAuth.getInstance().currentUser?.uid, selectedUserID))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Listen", "Error listening for messages: $e")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    // Process and display each new message
                    for (doc in snapshots.documentChanges) {
                        if (doc.type == DocumentChange.Type.ADDED) {
                            val message = doc.document.data
                            val messageContainer = findViewById<LinearLayout>(R.id.messageContainer)
                            processMessage(message, messageContainer)
                        }
                    }
                }
            }
    }

    private fun stopListeningForMessages() {
        // Remove the message listener
        messageListener?.remove()
    }

    private fun sendMessage() {
        // Get the message text from the EditText
        val messageText: String = messageInput.text.toString().trim()

        // Check if the message is not empty
        if (messageText.isNotEmpty()) {
            // Get the sender's ID (current user's ID)
            val senderID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // Get the current timestamp
            val timestamp = System.currentTimeMillis()

            // Create a new message document in Firestore
            val message = hashMapOf(
                "sender" to senderID,
                "receiver" to selectedUserID,
                "message" to messageText,
                "timestamp" to timestamp
                // Add any other fields as needed
            )

            // Save the message to Firestore
            db.collection("messages")
                .add(message)
                .addOnSuccessListener {
                    // Message saved successfully
                    // You can add any UI update or notification here
                }
                .addOnFailureListener { e ->
                    // Failed to save message
                    // Handle the error appropriately
                }

            // Clear the EditText after sending the message
            messageInput.setText("")
        }
    }

    private fun loadMessages(selectedUserID: String) {
        // This method may not be needed anymore since we are using a real-time listener
    }

    private fun processMessage(
        message: Map<String, Any>,
        messageContainer: LinearLayout
    ) {
        // Extract message details from the map
        val messageText = message["message"] as String
        val senderID = message["sender"] as String

        // Inflate the chat message layout
        val messageView =
            LayoutInflater.from(this@ChatPersonActivity).inflate(R.layout.chat_message_layout, null)

        // Find views within the inflated layout
        val messageTextView: TextView = messageView.findViewById(R.id.message_text)
        val senderImage: ImageView = messageView.findViewById(R.id.message_sender_image)

        // Set message text
        messageTextView.text = messageText

        // Set background colors and gravity based on sender and receiver
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (senderID == currentUserID) {
            // Set layout parameters for sender messages (right-aligned, blue background)
            layoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            messageView.setBackgroundResource(R.drawable.bluemsg_bg)
            messageTextView.setTextColor(Color.WHITE)
        } else {
            // Set layout parameters for receiver messages (left-aligned, grey background)
            layoutParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            messageView.setBackgroundResource(R.drawable.whitemsg_bg)
            messageTextView.setTextColor(Color.BLACK)
        }
        messageView.layoutParams = layoutParams

        // Add the inflated layout to the message container
        messageContainer.addView(messageView)
    }
}

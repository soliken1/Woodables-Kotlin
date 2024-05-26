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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatPersonActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var selectedUserID: String
    private lateinit var selectedUsername: String
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var chatLayout: LinearLayout
    private lateinit var bkbtn: ImageView
    private lateinit var messagearea: LinearLayout

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
        bkbtn = findViewById(R.id.backbutton);

        bkbtn.setOnClickListener{ finish() }

        messageInput.requestFocus();

        messagearea = findViewById(R.id.messagearea);



        // Set OnClickListener to the send button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Load messages for the selected user
        loadMessages(selectedUserID)
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

            // Immediately update the UI with the new message
            updateUIWithNewMessage(message)

            // Save the message to Firestore
            FirebaseFirestore.getInstance().collection("messages")
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
        if (selectedUserID.isNotEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val currentUserId = user.uid

                // Initialize an empty list to store all messages
                val allMessages = mutableListOf<Map<String, Any>>()

                // Query Firestore for messages between the current user and the selected user
                db.collection("messages")
                    .whereEqualTo("sender", currentUserId)
                    .whereEqualTo("receiver", selectedUserID)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        // Add all messages sent by the current user to the list
                        for (document in queryDocumentSnapshots) {
                            val messageData = document.data
                            allMessages.add(messageData)
                        }

                        // Query Firestore for messages sent by the selected user to the current user
                        db.collection("messages")
                            .whereEqualTo("sender", selectedUserID)
                            .whereEqualTo("receiver", currentUserId)
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener { queryDocumentSnapshots1 ->
                                // Add all messages sent by the selected user to the list
                                for (document in queryDocumentSnapshots1) {
                                    val messageData = document.data
                                    allMessages.add(messageData)
                                }

                                // Sort all messages based on their timestamps
                                allMessages.sortBy { it["timestamp"] as Long }

                                // Get the LinearLayout container for messages
                                val messageContainer = findViewById<LinearLayout>(R.id.messageContainer)
                                messageContainer.removeAllViews() // Clear existing messages

                                // Iterate through the sorted messages and display them
                                allMessages.forEach { message ->
                                    // Process each message
                                    processMessage(message, messageContainer, currentUserId)
                                }
                            }
                            .addOnFailureListener { e -> Log.e("LoadMessages", "Error getting messages: ", e) }
                    }
                    .addOnFailureListener { e -> Log.e("LoadMessages", "Error getting messages: ", e) }
            }
        }
    }

    private fun processMessage(
        message: Map<String, Any>,
        messageContainer: LinearLayout,
        currentUserId: String
    ) {
        // Extract message details from the map
        val messageText = message["message"] as String
        val senderID = message["sender"] as String
        Log.d("Message", "Sender: $senderID, Message: $messageText")

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
        if (senderID == currentUserId) {
            // Set layout parameters for sender messages (right-aligned, green background)
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

    private fun updateUIWithNewMessage(message: Map<String, Any>) {
        // Get the LinearLayout container for messages
        val messageContainer = findViewById<LinearLayout>(R.id.messageContainer)

        // Process the new message and add it to the UI
        processMessage(
            message,
            messageContainer,
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
        )
    }
}

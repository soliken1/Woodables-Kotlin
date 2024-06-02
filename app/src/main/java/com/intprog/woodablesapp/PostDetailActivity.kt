import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.LinearLayout
import android.widget.TextView
import com.intprog.woodablesapp.R

class PostDetailActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve intent extras
        val postId = intent.getStringExtra("postId")
        val postTitle = intent.getStringExtra("postTitle")
        val postMessage = intent.getStringExtra("postMessage")
        val postUser = intent.getStringExtra("postUser")

        // Set values to TextViews
        findViewById<TextView>(R.id.title).text = postTitle
        findViewById<TextView>(R.id.text).text = postMessage
        findViewById<TextView>(R.id.postusername).text = postUser

        // Add post details to the comment container
        val commentContainer: LinearLayout? = findViewById(R.id.comment_container)
        val inflater = layoutInflater

        // Inflate the post item layout
        val postDetailView = inflater.inflate(R.layout.post_item, null)

        // Populate the post item layout with post details
        val titleTextView: TextView = postDetailView.findViewById(R.id.title)
        val messageTextView: TextView = postDetailView.findViewById(R.id.text)
        val userNameTextView: TextView = postDetailView.findViewById(R.id.postusername)

        titleTextView.text = postTitle
        messageTextView.text = postMessage
        userNameTextView.text = postUser

        // Add the post detail view to the comment container
        commentContainer?.addView(postDetailView)
    }
}

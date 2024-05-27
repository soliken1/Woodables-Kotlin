    package com.intprog.woodablesapp
    
    import android.content.Intent
    import android.os.Bundle
    import android.os.SystemClock
    import android.view.View
    import android.widget.Button
    import android.widget.ImageView
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.graphics.Insets
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.FragmentManager
    import androidx.fragment.app.FragmentTransaction
    import com.google.firebase.auth.FirebaseAuth
    
    class AdminMainScreen : AppCompatActivity() {
    
        private lateinit var tolisting: Button
        private lateinit var toassessment: Button
        private lateinit var topost: Button
        private lateinit var logoutimg: ImageView
        private lateinit var mAuth: FirebaseAuth

        private val THROTTLE_MILLIS = 3000L // Adjust this value as needed (e.g., 1000L for 1 second)
        private var lastClickTime = 0L

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_admin_main_screen)
    
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
    
            // Initialize FirebaseAuth
            mAuth = FirebaseAuth.getInstance()
    
            tolisting = findViewById(R.id.toListings)
            toassessment = findViewById(R.id.toAssessment)
            topost = findViewById(R.id.toPosts)
            logoutimg = findViewById(R.id.logout) // Initialize the logout image view
    
            replaceFragment(AdminListingFragment())


            tolisting.setOnClickListener {
                throttleClick(AdminListingFragment::class.java)
            }

            toassessment.setOnClickListener {
                throttleClick(AdminAssesmentFragment::class.java)
            }

            topost.setOnClickListener {
                throttleClick(AdminPostFragment::class.java)
            }
    
            logoutimg.setOnClickListener {
                // Sign out from Firebase
                mAuth.signOut()
                // Navigate back to the LoginActivity and clear the back stack
                val intent = Intent(this@AdminMainScreen, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                // finish the current activity
                finish()
            }
        }
        private fun <T : Fragment> throttleClick(fragmentClass: Class<T>) {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - lastClickTime > THROTTLE_MILLIS) {
                lastClickTime = currentTime
                replaceFragment(fragmentClass.newInstance()) // Create and replace fragment instance
            }
        }
    
        private fun replaceFragment(frag: Fragment) {
            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
            fragmentTransaction.replace(R.id.contentViewAdmin, frag)
            fragmentTransaction.commit()
        }
    }

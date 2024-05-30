package com.intprog.woodablesapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class MainScreenActivity : AppCompatActivity() {

    private lateinit var homeclick: ImageView
    private lateinit var communityclick: ImageView
    private lateinit var postingclick: ImageView
    private lateinit var chatclick: ImageView
    private lateinit var docclick: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.wholeLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeclick = findViewById(R.id.home)
        communityclick = findViewById(R.id.community)
        postingclick = findViewById(R.id.hammer)
        chatclick = findViewById(R.id.messenger)
        docclick = findViewById(R.id.documents)

        // Get the role from the intent
        val role = intent.getStringExtra("ROLE")

        if ("client" == role) {
            replaceFragment(ClientProfileFragment())
            docclick.visibility = View.GONE
        } else {
            replaceFragment(WoodworkerProfileFragment())
        }

        homeclick.setOnClickListener {
            if ("client" == role) {
                replaceFragment(ClientProfileFragment())
            } else {
                replaceFragment(WoodworkerProfileFragment())
            }
            updateIcons(R.drawable.cinfill, R.drawable.socialicon, R.drawable.dghammer, R.drawable.dgmessage, R.drawable.dgdoc)
            enableAllBtn()
            homeclick.isEnabled = false
        }

        communityclick.setOnClickListener {
            replaceFragment(CommunityFragment())
            updateIcons(R.drawable.cinbtn, R.drawable.cinchat, R.drawable.dghammer, R.drawable.dgmessage, R.drawable.dgdoc)
            enableAllBtn()
            communityclick.isEnabled = false
        }

        postingclick.setOnClickListener {
            replaceFragment(if ("client" == role) CreateJobCardFragment() else JobListingFragment())
            updateIcons(R.drawable.cinbtn, R.drawable.socialicon, R.drawable.cinhammer, R.drawable.dgmessage, R.drawable.dgdoc)
            enableAllBtn()
            postingclick.isEnabled = false
        }

        chatclick.setOnClickListener {
            replaceFragment(MessageChatViewFragment())
            updateIcons(R.drawable.cinbtn, R.drawable.socialicon, R.drawable.dghammer, R.drawable.cinsocial, R.drawable.dgdoc)
            enableAllBtn()
            chatclick.isEnabled = false
        }

        docclick.setOnClickListener {
            replaceFragment(LearnCourseFragment())
            updateIcons(R.drawable.cinbtn, R.drawable.socialicon, R.drawable.dghammer, R.drawable.dgmessage, R.drawable.cindoc)
            enableAllBtn()
            docclick.isEnabled = false
        }
    }

    private fun updateIcons(homeRes: Int, communityRes: Int, postingRes: Int, chatRes: Int, docRes: Int) {
        homeclick.setImageResource(homeRes)
        communityclick.setImageResource(communityRes)
        postingclick.setImageResource(postingRes)
        chatclick.setImageResource(chatRes)
        docclick.setImageResource(docRes)
    }

    private fun replaceFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_left)
            replace(R.id.contentView, frag)
            commit()
        }
    }

    private fun enableAllBtn() {
        val nav = arrayOf(homeclick, communityclick, postingclick, chatclick, docclick)
        nav.forEach { it.isEnabled = true }
    }
}

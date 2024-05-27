package com.intprog.woodablesapp

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView

class MessageSettingActivity : AppCompatActivity() {

    private lateinit var backbtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_message_setting)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backbtn = findViewById(R.id.backbutton)

        backbtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
        }
    }
}
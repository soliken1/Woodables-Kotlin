package com.intprog.woodablesapp

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

class StartUpActivity : AppCompatActivity() {

    private lateinit var loginbtn: Button
    private lateinit var regbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_up)

        loginbtn = findViewById(R.id.loginBtn)
        regbtn = findViewById(R.id.regBtn)

        regbtn.setOnClickListener {
            val navreg = Intent(this@StartUpActivity, UserInfoActivity::class.java)
            startActivity(navreg)
        }

        loginbtn.setOnClickListener {
            val navlogin = Intent(this@StartUpActivity, LoginActivity::class.java)
            startActivity(navlogin)
        }
    }
}

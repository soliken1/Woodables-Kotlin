package com.intprog.woodablesapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class UserInfoActivity : AppCompatActivity() {

    private lateinit var woodType: RadioButton
    private lateinit var clientType: RadioButton
    private var selectedRole = "Woodworker" // Default role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_info)

        woodType = findViewById(R.id.workerrad)
        clientType = findViewById(R.id.clientrad)

        replaceFragment(UserInfoWoodworkerFragment())

        woodType.setOnClickListener {
            selectedRole = "woodworker"
            replaceFragment(UserInfoWoodworkerFragment())
        }

        clientType.setOnClickListener {
            selectedRole = "client"
            replaceFragment(UserInfoClientFragment())
        }
    }

    private fun replaceFragment(frag: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contentView, frag)
        fragmentTransaction.commit()
    }

    fun getSelectedRole(): String {
        return selectedRole
    }
}

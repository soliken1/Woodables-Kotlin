package com.intprog.woodablesapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

class UserInfoWoodworkerFragment : Fragment() {

    private lateinit var toReg: Button
    private lateinit var lNameText: EditText
    private lateinit var fNameText: EditText
    private lateinit var mNameText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRoot = inflater.inflate(R.layout.fragment_user_info_woodworker, container, false)

        lNameText = viewRoot.findViewById(R.id.lName)
        fNameText = viewRoot.findViewById(R.id.fName)
        mNameText = viewRoot.findViewById(R.id.mName)

        toReg = viewRoot.findViewById(R.id.toregbtn)

        toReg.setOnClickListener {
            val activity = requireActivity() as UserInfoActivity
            val toRegister = Intent(viewRoot.context, RegisterActivity::class.java)

            toRegister.putExtra("LName", lNameText.text.toString())
            toRegister.putExtra("FName", fNameText.text.toString())
            toRegister.putExtra("MName", mNameText.text.toString())
            toRegister.putExtra("Role", activity.getSelectedRole())

            startActivity(toRegister)
        }

        return viewRoot
    }
}

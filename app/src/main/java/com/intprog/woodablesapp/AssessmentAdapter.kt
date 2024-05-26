package com.intprog.woodablesapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AssessmentAdapter(context: Context?, assessments: List<Assessment?>?) :
    ArrayAdapter<Assessment?>(
        context!!, 0, assessments!!
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val assessment = getItem(position)
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.item_assessment, parent, false)
        }
        val nameTextView = convertView!!.findViewById<TextView>(R.id.assessmentName)
        val courseTextView = convertView.findViewById<TextView>(R.id.assessmentCourse)
        nameTextView.text = assessment!!.firstName + " " + assessment.lastName
        courseTextView.text = assessment.course
        Log.d("Adapter", "Binding assessment: " + assessment.firstName + " " + assessment.lastName)
        return convertView
    }
}
package com.example.areyuhere

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class TeacherHomeFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var code_generate_button:Button
    private lateinit var code_display: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacherhome, container, false)
        code_generate_button = view.findViewById(R.id.code_generate)
        code_display = view.findViewById(R.id.code_display)
        code_display.visibility = View.GONE

        code_generate_button.setOnClickListener{
            viewModel.newCode()
            code_display.text = viewModel.getC()
            code_generate_button.visibility = View.GONE
            code_display.visibility = View.VISIBLE
        }
        return view
    }
}
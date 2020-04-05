package com.example.areyuhere

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
private const val TAG = "StudentHome"

class StudentHomeFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var check_in_button:Button
    private lateinit var code_edittext:EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_studenthome, container, false)

        check_in_button = view.findViewById(R.id.checkin_button)
        code_edittext = view.findViewById(R.id.code)

        check_in_button.setOnClickListener{
            Log.d(TAG,"Check in clicked")
            if(code_edittext.text.toString().equals(viewModel.code)) {
                Log.d(TAG,"Navigating to student checkout")
                Navigation.createNavigateOnClickListener(R.id.action_studentHomeFragment_to_studentCheckOutFragment)
                view.findNavController().navigate(R.id.action_studentHomeFragment_to_studentCheckOutFragment)
            }
        }
        return view
    }
}
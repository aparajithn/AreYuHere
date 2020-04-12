package com.example.areyuhere

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.log

private const val TAG = "SigninFragment"

class SignInFragment :Fragment(){
    private lateinit var loginButton: Button
    private lateinit var username:EditText
    private lateinit var password:EditText


    val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            val view = inflater.inflate(R.layout.fragment_signin, container, false)
        loginButton =view.findViewById(R.id.login_button)
        username = view.findViewById(R.id.username)
        password = view.findViewById(R.id.password)

        loginButton.setOnClickListener{
            // Write a message to the database
            val database = Firebase.database
            val myRef = database.getReference("message")
            myRef.setValue("Hello, World!")
            Log.d(TAG,"Log in pressed")
            Log.d(TAG, username.text.toString())
            if(username.text.toString().equals(viewModel.userName) && password.text.toString().equals(viewModel.password)) {
                if(!viewModel.status) {
                    Log.d(TAG,"Navigate to student home")
                    Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_studentHomeFragment)
                    view.findNavController().navigate(R.id.action_signInFragment_to_studentHomeFragment)
                }
            }
            else if(username.text.toString().equals(viewModel.userName1) && password.text.toString().equals(viewModel.password1)) {
                if(viewModel.status1) {
                    Log.d(TAG,"Navigate to teacher home")
                    Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_teacherHomeFragment)
                    view.findNavController().navigate(R.id.action_signInFragment_to_teacherHomeFragment)
                }
            }
            else {
                Toast.makeText(context, "Enter valid credentials!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view

    }

}
package com.example.areyuhere.login

import android.os.Bundle
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
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth


class SignUpFragment:Fragment() {
    private lateinit var fullNameEditText: EditText
    private lateinit var displayNameEditText:EditText
    private lateinit var emailEditText:EditText
    private lateinit var passwordEditText:EditText
    private lateinit var signupButton:Button
    private lateinit var teacherSwitch:SwitchMaterial
    private lateinit var auth: FirebaseAuth

    val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        fullNameEditText = view.findViewById(R.id.name_signup)
        emailEditText = view.findViewById(R.id.email_signup)
        passwordEditText = view.findViewById(R.id.password_signup)
        displayNameEditText = view.findViewById(R.id.displayname)
        signupButton = view.findViewById(R.id.signup_button)
        teacherSwitch = view.findViewById(R.id.teacher_slider)

        auth = FirebaseAuth.getInstance()


        signupButton.setOnClickListener {
            //displays a toast if any fields are missing text
            //TODO: switch from toast to error icons/messages like on sign in
            if(emailEditText.text.toString().isNullOrEmpty() || fullNameEditText.text.toString().isNullOrEmpty() || passwordEditText.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    context, "Fill in all fields!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                //if all fields are filled, then add user info to auth and navigate to the sign in page
                signupAuth()
                Navigation.createNavigateOnClickListener(R.id.action_signUpFragment_to_signInFragment)
                view.findNavController()
                    .navigate(R.id.action_signUpFragment_to_signInFragment)
            }
        }


            return view
    }

    //Add new user to firebase auth
    fun signupAuth(){
        auth.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(context, "Sign up successful!",
                        Toast.LENGTH_SHORT).show()
                    //sign up was successful, so lets put new user into db as well
                    signupDb()
                }
                else{
                    Toast.makeText(context, "Sign up failed!",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Add user to realtime db
    fun signupDb(){
        var index = auth.currentUser?.uid
        val userData: MutableMap<String, Any> = HashMap()

        userData["email"] = emailEditText.text.toString()
        userData["name"] = fullNameEditText.text.toString()
        userData["preferred display name"] = displayNameEditText.text.toString()

        //this makes sure that students and teachers each get added to the correct node in db
        if (teacherSwitch.isChecked)
        {
            viewModel.teacherListRef.child(index.toString()).updateChildren(userData)
        }
        else
        {
            viewModel.studentListRef.child(index.toString()).updateChildren(userData)
        }
    }
}
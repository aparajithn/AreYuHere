package com.example.areyuhere

import android.R.attr.name
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase


private const val TAG = "SignupFragment"

class SignUpFragment:Fragment() {
    private lateinit var name_edittext:EditText
    private lateinit var email_edittext:EditText
    private lateinit var password_edittext:EditText
    private lateinit var signup_button:Button
    private lateinit var auth: FirebaseAuth

    val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        name_edittext = view.findViewById(R.id.name_signup)
        email_edittext = view.findViewById(R.id.email_signup)
        password_edittext = view.findViewById(R.id.password_signup)
        signup_button = view.findViewById(R.id.signup_button)
        auth = FirebaseAuth.getInstance()


        signup_button.setOnClickListener {

            if(email_edittext.text.toString().isNullOrEmpty() || name_edittext.text.toString().isNullOrEmpty() || password_edittext.text.toString().isNullOrEmpty())
                Toast.makeText(context, "Fill in all fields!",
                    Toast.LENGTH_SHORT).show()
            else
                signup_auth()
        }
        return view
    }
    //Add user to firebase auth
    fun signup_auth(){
        auth.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(context, "Sign up successful!",
                        Toast.LENGTH_SHORT).show()
                    signup_db()
                }
                else{
                    Toast.makeText(context, "Sign up failed!",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    //Add user to realtime db
    fun signup_db(){
        var index = viewModel.children_count + 1
        val userData: MutableMap<String, Any> = HashMap()

        userData["email"] = email_edittext.text.toString()
        userData["name"] = name_edittext.text.toString()
        userData["status"] = "F"

       viewModel.getStatus.child(index.toString()).updateChildren(userData)
    }

}
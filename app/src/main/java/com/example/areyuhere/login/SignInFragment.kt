package com.example.areyuhere.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_signin.*


private const val TAG = "SigninFragment"

class SignInFragment :Fragment() {
    private lateinit var loginButton: Button
    //'username' is the user's email
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var signupLink: TextView

    var teacherEmail = ""
    var teacherEmails = mutableListOf<String>()
    private var flagIsTeacher = false

    val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signin, container, false)
        loginButton = view.findViewById(R.id.login_button)
        username = view.findViewById(R.id.username)
        password = view.findViewById(R.id.password)
        signupLink = view.findViewById(R.id.signup_link)
        auth = FirebaseAuth.getInstance()

        /*
        * purpose: Get all teacher emails and put them into a list
        * inputs: a snapshot (subset) of the database starting from the node Referenced by teacherListRef
        * outputs: all teacher emails currently in the bd will be put into mutable list teacherEmails
        * TODO:
        */
        viewModel.teacherListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    for (snapshot2 in snapshot.children) {
                        if (snapshot2.key.toString() == "email") {
                            teacherEmail = snapshot2.value.toString()
                            teacherEmails.add(teacherEmail)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        loginButton.setOnClickListener {
            //only attempt to sign in if both email and pw fields are filled in
            if (!username.text.toString().isNullOrEmpty() && !password.text.toString().isNullOrEmpty()) {
                auth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI and viewmodel with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            viewModel.currentEmail = user?.email.toString()

                            //iterator to go through list of teacher emails
                            val teachersIterator = teacherEmails.iterator()
                            flagIsTeacher = false
                            //mark flag as true if user trying to log in is a teacher
                            while (teachersIterator.hasNext()) {
                                if (username.text.toString() == teachersIterator.next()) {
                                    flagIsTeacher = true
                                }
                            }
                            //navigate to appropriate home page depending on whether the user is a student or teacher
                            if (flagIsTeacher) {
                                Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_teacherHomeFragment)
                                view.findNavController()
                                    .navigate(R.id.action_signInFragment_to_teacherHomeFragment)
                            } else {
                                Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_studentHomeFragment)
                                view.findNavController()
                                    .navigate(R.id.action_signInFragment_to_studentHomeFragment)
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            password.error = "Authentication Failed"
                            password.requestFocus()
                            password_textField.endIconMode = END_ICON_NONE
                        }
                    }

            } else {
                //if at least one of the email/pw fields are not filled in, display an error message/icon
                if(password.text.isNullOrEmpty()){
                    password.error = "Enter a valid password"
                    password_textField.endIconMode = END_ICON_NONE
                    password.requestFocus()
                }
                if(username.text.isNullOrEmpty()) {
                    username.error = "Enter a valid email"
                    username.requestFocus()
                }
            }
        }

        //navigate to SocialsSignUpFragment on click
        signupLink.setOnClickListener {
            Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_socialsSignUpFragment)
            view.findNavController().navigate(R.id.action_signInFragment_to_socialsSignUpFragment)
        }

        //clicking the eye (end icon on pw field) will toggle visibility of text
        password.setOnClickListener{
            password_textField.endIconMode = END_ICON_PASSWORD_TOGGLE
        }

        return view
    }
}
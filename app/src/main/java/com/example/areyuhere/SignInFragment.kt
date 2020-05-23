package com.example.areyuhere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


private const val TAG = "SigninFragment"

class SignInFragment :Fragment() {
    private lateinit var loginButton: Button
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var signupTextView: TextView


    var teacher_email = ""
    var teacherEmails = mutableListOf<String>()
    private var counter = 0
    var flagIsTeacher = false


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
        signupTextView = view.findViewById(R.id.signup_link)
        auth = FirebaseAuth.getInstance()


//Get teacher emails and put into a list
        viewModel.teacherListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    for (snapshot2 in snapshot.children) {
                        if (snapshot2.key.toString() == "email") {
                            teacher_email = snapshot2.value.toString()
                            Log.d(TAG, teacher_email)
                            teacherEmails.add(teacher_email)
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        loginButton.setOnClickListener {
            if (!username.text.toString().isNullOrEmpty() && !password.text.toString()
                    .isNullOrEmpty()
            ) {
                auth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            viewModel.currentEmail = user?.email.toString()

                            val teachersIterator = teacherEmails.iterator()
                            flagIsTeacher = false
                            while (teachersIterator.hasNext()) {
                                if (username.text.toString().equals(teachersIterator.next())) {
                                    flagIsTeacher = true
                                }
                            }
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
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                context, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            } else {
                Toast.makeText(
                        context, "Enter a username & password",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
        signupTextView.setOnClickListener {
            Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_socialsSignUpFragment)
            view.findNavController().navigate(R.id.action_signInFragment_to_socialsSignUpFragment)
        }



        return view

    }
}

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlin.math.log

private const val TAG = "SigninFragment"

class SignInFragment :Fragment(){
    private lateinit var loginButton: Button
    private lateinit var username:EditText
    private lateinit var password:EditText
    private lateinit var auth: FirebaseAuth
    var teacher_email = ""
    private var counter = 0


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
        auth = FirebaseAuth.getInstance()
        viewModel.isTeacher.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                 teacher_email = value.toString()

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
        viewModel.getStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.children_count = dataSnapshot.childrenCount
                if (viewModel.userList.isNullOrEmpty()){
                    for (snapshot in dataSnapshot.children) {
                        val user = User()
                        user.id = snapshot.key.toString()
                        counter = 0
                        for (s2 in snapshot.children) {
                            if (counter == 1) {
                                user.name = s2.value.toString()
                            }
                            if (counter == 2) {
                                user.isCheckedin = s2.value.toString()
                            }


                            counter++
                        }
                        viewModel.userList += user
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        loginButton.setOnClickListener{
            auth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        viewModel.currentEmail=user?.email.toString()
                        if(username.text.toString() == teacher_email){
                            Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_teacherHomeFragment)
                            view.findNavController().navigate(R.id.action_signInFragment_to_teacherHomeFragment)
                        }
                        else
                        {
                            Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_studentHomeFragment)
                            view.findNavController().navigate(R.id.action_signInFragment_to_studentHomeFragment)
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
//            Log.d(TAG,"Log in pressed")
//            Log.d(TAG, username.text.toString())
//            if(username.text.toString().equals(viewModel.userName) && password.text.toString().equals(viewModel.password)) {
//                if(!viewModel.status) {
//                    Log.d(TAG,"Navigate to student home")
//                    Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_studentHomeFragment)
//                    view.findNavController().navigate(R.id.action_signInFragment_to_studentHomeFragment)
//                }
//            }
//            else if(username.text.toString().equals(viewModel.userName1) && password.text.toString().equals(viewModel.password1)) {
//                if(viewModel.status1) {
//                    Log.d(TAG,"Navigate to teacher home")
//                    Navigation.createNavigateOnClickListener(R.id.action_signInFragment_to_teacherHomeFragment)
//                    view.findNavController().navigate(R.id.action_signInFragment_to_teacherHomeFragment)
//                }
//            }
//            else {
//                Toast.makeText(context, "Enter valid credentials!", Toast.LENGTH_SHORT)
//                    .show()
//            }
        }
        return view

    }

}
package com.example.areyuhere.student

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
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


private const val TAG = "StudentClass"

class StudentClassFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var checkInButton:Button
    private lateinit var codeEditText:EditText
    private lateinit var titleText:TextView
    private lateinit var checkinCode:String
    private lateinit var auth: FirebaseAuth
    private lateinit var status:String
    private var classTitleText = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_studentclass, container, false)

        checkInButton = view.findViewById(R.id.checkin_button)
        codeEditText = view.findViewById(R.id.code)
        titleText = view.findViewById(R.id.class_Title)
        auth = FirebaseAuth.getInstance()

        //gets the class title from the classes enrolled in inside  the current student and updates the title text accordingly
        viewModel.studentListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (snapshot.key.toString() == auth.currentUser?.uid)
                    {
                        for (s2 in snapshot.children)
                        {
                            if (s2.key.toString() == "enrolled classes")
                            {
                                for (s3 in s2.children)
                                {
                                    if (s3.key.toString() == viewModel.currentClass)
                                    {
                                        classTitleText = s3.value.toString()
                                        titleText.text = classTitleText
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        //Read code in database
        viewModel.classListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (s1 in dataSnapshot.children)
                {
                    if (s1.key.toString() == viewModel.currentClass)
                    {
                        for (s2 in s1.children)
                        {
                            if (s2.key.toString() == "code")
                            {
                                checkinCode = s2.key.toString()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //set student status to true for today if they typed in the correct code
        //TODO: make and set some value to true in viewmodel that lets student home know this student is already
        // checked in to this class so that in case they reopen app all they have to do is check out
        checkInButton.setOnClickListener{
            if(codeEditText.text.toString().equals(checkinCode) && !codeEditText.text.toString().isNullOrEmpty()) {
                viewModel.classListRef.child(viewModel.currentClass).child("enrolled students").child(auth.currentUser!!.uid).setValue("T")
                Navigation.createNavigateOnClickListener(R.id.action_studentClassFragment_to_studentCheckOutFragment)
                view.findNavController().navigate(R.id.action_studentClassFragment_to_studentCheckOutFragment)
                Toast.makeText(context, "Checked in!", Toast.LENGTH_SHORT)
                    .show()
            }
            else{
                Toast.makeText(context, "Check in failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view
    }
}
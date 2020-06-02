package com.example.areyuhere.student

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
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


private const val TAG = "StudentHome"

class StudentClassFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var check_in_button:Button
    private lateinit var code_edittext:EditText
    private lateinit var checkin_code:String
    private lateinit var auth: FirebaseAuth
    private lateinit var status:String
    var flag = false
    var i_d = ""
    var realID = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_studentclass, container, false)

        check_in_button = view.findViewById(R.id.checkin_button)
        code_edittext = view.findViewById(R.id.code)
        //Read code in database
        viewModel.codeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                checkin_code = value.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        viewModel.studentListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG,dataSnapshot.childrenCount.toString())
                    for (snapshot in dataSnapshot.children) {
                        i_d=snapshot.key.toString()
                        for(s2 in snapshot.children){
                            if(s2.value==viewModel.currentEmail){
                                status = s2.value.toString()
                                viewModel.id=i_d
                            }

                            status = s2.value.toString()
                        }

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })


        check_in_button.setOnClickListener{
            if(code_edittext.text.toString().equals(checkin_code) && !code_edittext.text.toString().isNullOrEmpty()) {
                Log.d(TAG,"Navigating to student checkout")
                viewModel.studentRef.child(viewModel.id).child("status").setValue("T")
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
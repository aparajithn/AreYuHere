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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private const val TAG = "StudentHome"

class StudentHomeFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var check_in_button:Button
    private lateinit var code_edittext:EditText
    private lateinit var checkin_code:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_studenthome, container, false)

        check_in_button = view.findViewById(R.id.checkin_button)
        code_edittext = view.findViewById(R.id.code)
        viewModel.code_ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                checkin_code = value.toString()

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })


        check_in_button.setOnClickListener{
            val database = Firebase.database
            val myRef = database.getReference("message")
            myRef.setValue("Hello, World2!")
            Log.d(TAG,"Check in clicked")
            if(code_edittext.text.toString().equals(checkin_code)) {
                Log.d(TAG,"Navigating to student checkout")
                Navigation.createNavigateOnClickListener(R.id.action_studentHomeFragment_to_studentCheckOutFragment)
                view.findNavController().navigate(R.id.action_studentHomeFragment_to_studentCheckOutFragment)
            }
        }
        return view
    }
}
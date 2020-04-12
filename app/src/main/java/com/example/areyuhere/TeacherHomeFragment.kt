package com.example.areyuhere

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class TeacherHomeFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var code_generate_button:Button
    private lateinit var code_display: TextView
    private var code =""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacherhome, container, false)
        code_generate_button = view.findViewById(R.id.code_generate)
        code_display = view.findViewById(R.id.code_display)
        code_display.visibility = View.GONE
        viewModel.code_ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                code_display.text = value

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

        code_generate_button.setOnClickListener{
            viewModel.newCode()
            code_generate_button.visibility = View.GONE
            code_display.visibility = View.VISIBLE
        }
        return view
    }
}
package com.example.areyuhere.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel

class StudentCheckOutFragment : Fragment(){
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var checkoutButton:Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_studentcheckout, container, false)
        checkoutButton = view.findViewById(R.id.checkout_button)
        //TODO: this is for old implementation. update for new
        checkoutButton.setOnClickListener{
            viewModel.studentRef.child(viewModel.id).child("status").setValue("F")
            Navigation.createNavigateOnClickListener(R.id.action_studentCheckOutFragment_to_signInFragment)
            view.findNavController().navigate(R.id.action_studentCheckOutFragment_to_signInFragment)
        }
        return view
    }
}
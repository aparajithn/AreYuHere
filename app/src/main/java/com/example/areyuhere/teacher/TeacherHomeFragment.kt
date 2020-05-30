package com.example.areyuhere.teacher

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
private var flag = false

class TeacherHomeFragment: Fragment() {
    val uidListC = mutableListOf<String>()
    private lateinit var auth: FirebaseAuth
    private lateinit var addClassroom:Button

    val viewModel: UserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacherhome, container, false)
        addClassroom = view.findViewById(R.id.add_classroom)
        auth = FirebaseAuth.getInstance()
        viewModel.classListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    uidListC.add(snapshot.key.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })



        addClassroom.setOnClickListener{
            var index = viewModel.newUID()
            val uidIteratorC = uidListC.iterator()
            flag = true
            while(flag) {
                flag = false
                while (uidIteratorC.hasNext()) {
                    if (index.equals(uidIteratorC.next())) {
                        index = viewModel.newUID()
                        flag = true
                    }

                }
            }
            val classData: MutableMap<String, Any> = HashMap()
            classData["code"] = "test"
            classData["name"] = "Communicatio Networks"
            classData["pw"] = "test123"
            classData["teacher"] = auth.currentUser?.uid.toString()
            viewModel.classRef.child(index).updateChildren(classData)

        }


        return view
    }
}
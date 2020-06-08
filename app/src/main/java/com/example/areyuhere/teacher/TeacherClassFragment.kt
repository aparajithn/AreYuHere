package com.example.areyuhere.teacher

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areyuhere.R
import com.example.areyuhere.User
import com.example.areyuhere.UserViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

private const val TAG = "TeacherClass"

class TeacherClassFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var codeGenerateButton:Button
    private lateinit var codeDisplay:TextView
    private lateinit var userListRecyclerView:RecyclerView
    private lateinit var codeExpiry:TextView
    private lateinit var classTitle:TextView
    private var adapter: UserAdapter?=null
    private var counter = 0
    private var flag = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacherclass, container, false)
        userListRecyclerView = view.findViewById(R.id.studentList) as RecyclerView
        userListRecyclerView.layoutManager = LinearLayoutManager(context)
        codeGenerateButton = view.findViewById(R.id.code_generate)
        codeDisplay = view.findViewById(R.id.code_display)
        codeExpiry= view.findViewById(R.id.code_expiry)
        codeExpiry.visibility = View.GONE

        classTitle = view.findViewById(R.id.class_Title)
        classTitle.text = viewModel.currentClass

        val timer = object: CountDownTimer(100000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                codeExpiry.visibility = View.VISIBLE
                codeExpiry.text = "Your code will expire in ${millisUntilFinished/1000} seconds"
            }

            override fun onFinish() {
                viewModel.codeRef.setValue("")
                codeExpiry.text = R.string.expiry_text.toString()
            }
        }

        viewModel.studentListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.userList.clear()
                viewModel.childrenCount = dataSnapshot.childrenCount
                if(viewModel.userList.isNullOrEmpty()) {
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
                        Log.d(TAG, user.toString())
                        updateUI()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        viewModel.codeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                codeDisplay.text = value

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })


        codeGenerateButton.setOnClickListener{
            val dialogBuilder = context?.let { it1 -> AlertDialog.Builder(it1) }

            // set message of alert dialog
            dialogBuilder?.setMessage(R.string.message)
                // if the dialog is cancelable
                ?.setCancelable(false)
                // positive button text and action
                ?.setPositiveButton(R.string.yes, DialogInterface.OnClickListener {
                        dialog, id -> viewModel.resetStatus()
                    dialog.cancel()
                })
                // negative button text and action
                ?.setNegativeButton(R.string.no, DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder?.create()
            // set title for alert dialog box
            alert?.setTitle("Reset Status")
            // show alert dialog
            alert?.show()
            viewModel.newCode()
            timer.start()
        }
        updateUI()
        return view
    }
    private fun updateUI(){
        val users = viewModel.userList
        Log.d(TAG,users.toString())
        adapter = UserAdapter(users)
        userListRecyclerView.adapter = adapter
    }
    private inner class UserHolder(view:View)
        :RecyclerView.ViewHolder(view),View.OnClickListener{
        private lateinit var user: User
        private val nameTextView:TextView = itemView.findViewById(R.id.student_name)
        private val isCheckedIn: ImageView = itemView.findViewById(R.id.isCheckedIn)
        private val isNotCheckedIn: ImageView = itemView.findViewById(R.id.isNotCheckedIn)


        init{
            nameTextView.setOnClickListener(this)
        }

        fun bind(user: User){
            this.user = user
            nameTextView.text = this.user.name
            isCheckedIn.visibility = if(user.isCheckedin == "T"){
                View.VISIBLE
            } else{
                View.GONE
            }
            isNotCheckedIn.visibility = if(user.isCheckedin == "F"){
                View.VISIBLE
            } else{
                View.GONE
            }
        }
//RECYCLER ITEM KEY PRESS
        override fun onClick(v: View?) {
            val input = nameTextView.text
            val dialogBuilder = context?.let { it1 -> AlertDialog.Builder(it1) }

            // set message of alert dialog
            dialogBuilder?.setMessage("Would you like to change status of $input?")
                // if the dialog is cancelable
                ?.setCancelable(false)
                // positive button text and action
                ?.setPositiveButton(R.string.yes, DialogInterface.OnClickListener {
                        dialog, id ->
                    viewModel.userList.forEach {
                    if(it.name == input){
                        if(it.isCheckedin=="F"){
                            viewModel.studentRef.child(it.id).child("status").setValue("T")
                            flag = 1
                        }
                        else {
                            viewModel.studentRef.child(it.id).child("status").setValue("F")
                            flag = 0
                        }
                    }
                }
                    dialog.cancel()
                    if(flag ==0) {
                        Toast.makeText(context, "$input has been checked out", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else if(flag == 1){
                        Toast.makeText(context, "$input has been checked in", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
                // negative button text and action
                ?.setNegativeButton(R.string.no, DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder?.create()
            // set title for alert dialog box
            alert?.setTitle("Change Status")
            // show alert dialog
            alert?.show()

        }
    }
    private inner class UserAdapter(var users:List<User>)
        :RecyclerView.Adapter<UserHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
            val view = layoutInflater.inflate(R.layout.list_item_student,parent,false)
            return UserHolder(view)
        }

        override fun getItemCount() = users.size



        override fun onBindViewHolder(holder: UserHolder, position: Int) {
            val user = users[position]
            holder.bind(user)
        }

    }

}
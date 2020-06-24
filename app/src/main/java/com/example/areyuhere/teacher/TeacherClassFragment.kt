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
import com.example.areyuhere.Class
import com.example.areyuhere.R
import com.example.areyuhere.User
import com.example.areyuhere.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

private const val TAG = "TeacherClass"

class TeacherClassFragment : Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var codeGenerateButton: Button
    private lateinit var codeDisplay: TextView
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var codeExpiry: TextView
    private lateinit var classTitle: TextView
    private lateinit var auth: FirebaseAuth
    private var adapter: UserAdapter? = null
    private var flag = 0
    private var classTitleText = ""

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
        codeExpiry = view.findViewById(R.id.code_expiry)
        codeExpiry.visibility = View.GONE

        classTitle = view.findViewById(R.id.class_Title)

        auth = FirebaseAuth.getInstance()

        //countdown timer for code expiry
        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                codeExpiry.visibility = View.VISIBLE
                codeExpiry.text = "Your code will expire in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                viewModel.classListRef.child(viewModel.currentClass).child("code").setValue("")
                codeExpiry.text = "Your code has expired!"
            }
        }

        //gets the class title from the classes taught list inside  the current teacher and updates the title text accordingly
        viewModel.teacherListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (snapshot.key.toString() == auth.currentUser?.uid) {
                        for (s2 in snapshot.children) {
                            if (s2.key.toString() == "classes taught") {
                                for (s3 in s2.children) {
                                    if (s3.key.toString() == viewModel.currentClass) {
                                        classTitleText = s3.value.toString()
                                        classTitle.text = classTitleText
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        //gets this class's enrolled students list and adds it to recycler view via viewmodel middleman. Also updates codeDisplay's text and visibility status TODO: make qr code display
        viewModel.classListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.userList.clear()
                for (s1 in dataSnapshot.children) {
                    if (s1.key.toString() == viewModel.currentClass) {
                        for (s2 in s1.children) {
                            if (s2.key.toString() == "code") {
                                codeDisplay.text = s2.value.toString()
                            } else if (s2.key.toString() == "enrolled students") {
                                viewModel.childrenCount = s2.childrenCount
                                for (s3 in s2.children) {
                                    val user = User()
                                    user.id = s3.key.toString()
                                    for (s4 in s3.children) {
                                        if (s4.key.toString() == "name") {
                                            user.name = s4.value.toString()
                                        }
                                        if (s4.key.toString() == "status") {
                                            user.isCheckedin = s4.value.toString()
                                        }
                                    }
                                    viewModel.userList += user
                                    updateUI()
                                }
                            }
                            else if (s2.key.toString() == "verification method")
                            {
                                if (s2.value.toString() == "alphanumeric")
                                {
                                    codeDisplay.visibility = View.VISIBLE
                                }
                                else if (s2.value.toString() == "qr")
                                {
                                    codeDisplay.visibility = View.GONE  //View.INVISIBLE will still take up layout space. TODO: figure out which is needed gone or invisible
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        codeGenerateButton.setOnClickListener {
            if (adapter!!.itemCount == 0) {
                val newCode = viewModel.newCode()
                viewModel.classListRef.child(viewModel.currentClass).child("code").setValue(newCode)
                timer.start()
            } else {

                val dialogBuilder = context?.let { it1 -> AlertDialog.Builder(it1) }

                // set message of alert dialog
                dialogBuilder?.setMessage(R.string.message)
                    // if the dialog is cancelable
                    ?.setCancelable(false)
                    // positive button text and action
                    ?.setPositiveButton(
                        R.string.yes,
                        DialogInterface.OnClickListener { dialog, id ->
                            viewModel.resetStatus()
                            dialog.cancel()
                        })
                    // negative button text and action
                    ?.setNegativeButton(R.string.no, DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

                // create dialog box
                val alert = dialogBuilder?.create()
                // set title for alert dialog box
                alert?.setTitle("Reset Status")
                // show alert dialog
                alert?.show()

                //generates a new code and starts the timer for the code expiry only once the alert is closed
                alert?.setOnCancelListener {
                    val newCode = viewModel.newCode()
                    viewModel.classListRef.child(viewModel.currentClass).child("code")
                        .setValue(newCode)
                    timer.start()
                }
            }
        }
        updateUI()
        return view
    }

    private fun updateUI() {
        val users = viewModel.userList

        adapter = UserAdapter(users)
        userListRecyclerView.adapter = adapter
    }

    private inner class UserHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var user: User
        private val nameTextView: TextView = itemView.findViewById(R.id.student_name)
        private val isCheckedIn: ImageView = itemView.findViewById(R.id.isCheckedIn)
        private val isNotCheckedIn: ImageView = itemView.findViewById(R.id.isNotCheckedIn)


        init {
            nameTextView.setOnClickListener(this)
        }

        fun bind(user: User) {
            this.user = user
            nameTextView.text = this.user.name
            isCheckedIn.visibility = if (user.isCheckedin == "T") {
                View.VISIBLE
            } else {
                View.GONE
            }
            isNotCheckedIn.visibility = if (user.isCheckedin == "F") {
                View.VISIBLE
            } else {
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
                ?.setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialog, id ->
                    viewModel.userList.forEach {
                        if (it.name == input) {

                            if (it.isCheckedin == "F") {
                                viewModel.classListRef.child(viewModel.currentClass)
                                    .child("enrolled students").child(it.id).child("status")
                                    .setValue("T")
                                flag = 1
                            } else {
                                viewModel.classListRef.child(viewModel.currentClass)
                                    .child("enrolled students").child(it.id).child("status")
                                    .setValue("F")
                                flag = 0
                            }
                        }
                    }
                    dialog.cancel()
                    if (flag == 0) {
                        Toast.makeText(context, "$input has been checked out", Toast.LENGTH_SHORT)
                            .show()
                    } else if (flag == 1) {
                        Toast.makeText(context, "$input has been checked in", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
                // negative button text and action
                ?.setNegativeButton(R.string.no, DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder?.create()
            // set title for alert dialog box
            alert?.setTitle("Change Status")
            // show alert dialog
            alert?.show()

        }
    }

    private inner class UserAdapter(var users: List<User>) : RecyclerView.Adapter<UserHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
            val view = layoutInflater.inflate(R.layout.list_item_student, parent, false)
            return UserHolder(view)
        }

        override fun getItemCount() = users.size


        override fun onBindViewHolder(holder: UserHolder, position: Int) {

            val user = users[position]
            holder.bind(user)

        }

    }

}
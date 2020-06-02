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

private const val TAG = "TeacherHome"
private val CSV_HEADER = "id,name,status"

class TeacherClassFragment:Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var code_generate_button:Button
    private lateinit var code_display: TextView
    private lateinit var userListRecyclerView:RecyclerView
    private lateinit var code_expiry:TextView
    private lateinit var export_data:TextView
    private var adapter: UserAdapter?=null
    private var code =""
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
        code_generate_button = view.findViewById(R.id.code_generate)
        code_display = view.findViewById(R.id.code_display)
        export_data = view.findViewById(R.id.export_data)
        code_expiry= view.findViewById(R.id.code_expiry)
        code_expiry.visibility = View.GONE
        val timer = object: CountDownTimer(100000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                code_expiry.visibility = View.VISIBLE
                code_expiry.text = "Your code will expire in ${millisUntilFinished/1000} seconds"
            }

            override fun onFinish() {
                viewModel.codeRef.setValue("")
                code_expiry.text = "Your code has expired"
            }
        }
        viewModel.studentListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.userList.clear()
                viewModel.children_count = dataSnapshot.childrenCount
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
                code_display.text = value

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })


        code_generate_button.setOnClickListener{
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
        export_data.setOnClickListener{
            export()
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

       private fun export() {
//
//           if (context?.let { ContextCompat.checkSelfPermission(it, "WRITE_EXTERNAL_STORAGE") }
//               != PackageManager.PERMISSION_GRANTED) {
//               if (ActivityCompat.shouldShowRequestPermissionRationale(
//                       context as Activity,
//                       "WRITE_EXTERNAL_STORAGE")) {
//                   // Show an explanation to the user *asynchronously* -- don't block
//                   // this thread waiting for the user's response! After the user
//                   // sees the explanation, try again to request the permission.
//               } else {
//                   // No explanation needed, we can request the permission.
//                   ActivityCompat.requestPermissions(context as Activity,
//                       arrayOf("WRITE_EXTERNAL_STORAGE") ,0)
//
//
//                   // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                   // app-defined int constant. The callback method gets the
//                   // result of the request.
//               }
//           } else {
//               // Permission has already been granted
//           }
//
//        var fileWriter: FileWriter? = null
//
//        try {
//            fileWriter = FileWriter("customer.csv")
//
//            fileWriter.append(CSV_HEADER)
//            fileWriter.append('\n')
//
//            for (users in viewModel.userList) {
//                fileWriter.append(users.id)
//                fileWriter.append(',')
//                fileWriter.append(users.name)
//                fileWriter.append(',')
//                fileWriter.append(users.isCheckedin)
//                fileWriter.append('\n')
//            }
//
//            Log.d(TAG,"Write CSV successfully!")
//        } catch (e: Exception) {
//            Log.d(TAG,"Writing CSV error!")
//            e.printStackTrace()
//        } finally {
//            try {
//                fileWriter!!.flush()
//                fileWriter.close()
//            } catch (e: IOException) {
//                Log.d(TAG,"Flushing/closing error!")
//                e.printStackTrace()
//            }
//        }
//    }

}}
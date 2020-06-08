package com.example.areyuhere.teacher

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.areyuhere.Class
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.NonCancellable.cancel
private const val TAG = "TeacherClassDialog"
private var flag = false

class TeacherClassDialog:DialogFragment() {
    val viewModel: UserViewModel by activityViewModels()
    val uidListC = mutableListOf<String>()
    private lateinit var classroomName:EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var password:EditText
    private lateinit var verificationStatus:RadioGroup
    private lateinit var submitButton:Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_create_class, container, false)
        classroomName = view.findViewById(R.id.dialog_classroomname)
        password = view.findViewById(R.id.dialog_password)
//        verificationStatus = view.findViewById(R.id.verification_status)
        submitButton = view.findViewById(R.id.submit_button)
        auth = FirebaseAuth.getInstance()

        viewModel.classListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.classList.clear()
                for (snapshot in dataSnapshot.children) {
                    uidListC.add(snapshot.key.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        submitButton.setOnClickListener{
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
            classData["code"] = "default"
            classData["name"] = classroomName.text.toString()
            classData["pw"] = password.text.toString()
            classData["teacher"] = auth.currentUser?.uid.toString()
            viewModel.classRef.child(index).updateChildren(classData)
        }
        return view
    }

//        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return activity?.let {
//            val builder = AlertDialog.Builder(it)
//            // Get the layout inflater
//            val inflater = requireActivity().layoutInflater
//            // Inflate and set the layout for the dialog
//            // Pass null as the parent view because its going in the dialog layout
//            val view = builder.setView(inflater.inflate(R.layout.dialog_create_class, null))
//                // Add action buttons
//                .setPositiveButton(R.string.yes,
//                    DialogInterface.OnClickListener { dialog, id ->
//
//                    })
//                .setNegativeButton(R.string.no,
//                    DialogInterface.OnClickListener { dialog, id ->
//
//                    })
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }
}
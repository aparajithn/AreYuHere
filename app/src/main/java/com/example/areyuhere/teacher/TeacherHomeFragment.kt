package com.example.areyuhere.teacher

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areyuhere.Class
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.dialog_create_class.*

private var flag = false

class TeacherHomeFragment : Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var addClassroom: FloatingActionButton
    private lateinit var teacherClassList: RecyclerView
    private lateinit var classroomName:EditText
    private lateinit var password:EditText
    private lateinit var qrCode:RadioButton
    private lateinit var alphanumericCode:RadioButton
    val uidListC = mutableListOf<String>()
    private var adapter: ClassAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacherhome, container, false)
        addClassroom = view.findViewById(R.id.add_classroom)
        teacherClassList = view.findViewById(R.id.recylerview_teacher_classes)
        teacherClassList.layoutManager =
            GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)
        teacherClassList.setHasFixedSize(true)
        auth = FirebaseAuth.getInstance()

        viewModel.classListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.classList.clear()
                for (snapshot in dataSnapshot.children) {
                    val clazz = Class()

                    clazz.cUID = snapshot.key.toString()
                    for (s2 in snapshot.children) {
                        if (s2.key.toString().equals("code")) {
                            clazz.code = s2.value.toString()
                        }
                        if (s2.key.toString().equals("name")) {
                            clazz.name = s2.value.toString()
                        }
                        if (s2.key.toString().equals("pw")) {
                            clazz.pw = s2.value.toString()
                        }
                        if (s2.key.toString().equals("teacher")) {
                            clazz.tUID = s2.value.toString()
                        }

                    }
                    if (clazz.tUID.equals(auth.currentUser?.uid.toString())) {
                        viewModel.classList.add(clazz)
                    }
                    updateUI()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })



        addClassroom.setOnClickListener {

            val factory = LayoutInflater.from(context)
            val createClassDialogView: View = factory.inflate(R.layout.dialog_create_class, null)
            val createClassDialog: AlertDialog = AlertDialog.Builder(context).create()
            createClassDialog.setView(createClassDialogView)
            classroomName = createClassDialogView.findViewById(R.id.dialog_classroomname)
            password = createClassDialogView.findViewById(R.id.dialog_password)
            qrCode = createClassDialogView.findViewById(R.id.qr_code)
            alphanumericCode = createClassDialogView.findViewById(R.id.alphanumeric_code)
            createClassDialogView.findViewById<View>(R.id.submit_button)
                .setOnClickListener {
                    addClass()

                    createClassDialog.dismiss()


                }
            createClassDialog.show()
        }
        updateUI()
        return view
    }

    private fun updateUI() {
        val classes = viewModel.classList
        adapter = ClassAdapter(classes)
        teacherClassList.adapter = adapter
    }

    private inner class ClassHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var c1: Class
        private val cardView: CardView = itemView.findViewById(R.id.list_item_card)
        private val classNameTextView: TextView = itemView.findViewById(R.id.class_name)


        init {
            cardView.setOnClickListener(this)
        }

        fun bind(c1: Class) {
            this.c1 = c1
            classNameTextView.text = this.c1.name

        }

        //RECYCLER ITEM KEY PRESS
        override fun onClick(v: View?) {
            viewModel.currentClass = this.c1.cUID
            viewModel.userList.clear()
            Navigation.createNavigateOnClickListener(R.id.action_teacherHomeFragment_to_teacherClassFragment)
            view?.findNavController()
                ?.navigate(R.id.action_teacherHomeFragment_to_teacherClassFragment)
        }
    }

    private inner class ClassAdapter(var classes: List<Class>) :
        RecyclerView.Adapter<ClassHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassHolder {
            val view = layoutInflater.inflate(R.layout.list_item_classrooms_card, parent, false)
            return ClassHolder(view)
        }

        override fun getItemCount() = classes.size


        override fun onBindViewHolder(holder: ClassHolder, position: Int) {
            val c1 = classes[position]
            holder.bind(c1)
        }

    }

    private fun addClass() {
        var index = viewModel.newUID()
        val uidIteratorC = uidListC.iterator()
        flag = true
        while (flag) {
            flag = false
            while (uidIteratorC.hasNext()) {
                if (index.equals(uidIteratorC.next())) {
                    index = viewModel.newUID()
                    flag = true
                }

            }
        }
        val classData: MutableMap<String, Any> = HashMap()
        val teacherData: MutableMap<String, Any> = HashMap()
        teacherData[index] = classroomName.text.toString()
        classData["code"] = "default"
        classData["name"] = classroomName.text.toString()
        classData["pw"] = password.text.toString()
        classData["teacher"] = auth.currentUser?.uid.toString()
        if (alphanumericCode.isChecked)
        {
            classData["verification method"] = "alphanumeric"
        }
        else //qr must be checked
        {
            classData["verification method"] = "qr"
        }
        viewModel.classRef.child(index).updateChildren(classData)
        viewModel.teacherListRef.child(auth.uid.toString()).child("classes taught")
            .updateChildren(teacherData)
    }
}
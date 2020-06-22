package com.example.areyuhere.teacher

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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


class TeacherHomeFragment: Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var addClassroom:FloatingActionButton
    private lateinit var teacherClassList: RecyclerView
    private var adapter : ClassAdapter?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacherhome, container, false)
        addClassroom = view.findViewById(R.id.add_classroom)
        teacherClassList = view.findViewById(R.id.recylerview_teacher_classes)
        teacherClassList.layoutManager = GridLayoutManager(context,2,LinearLayoutManager.VERTICAL,false)
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
                        if(s2.key.toString().equals("name")){
                            clazz.name = s2.value.toString()
                        }
                        if(s2.key.toString().equals("pw")){
                            clazz.pw = s2.value.toString()
                        }
                        if(s2.key.toString().equals("teacher")){
                            clazz.tUID = s2.value.toString()
                        }

                    }
                    if(clazz.tUID.equals(auth.currentUser?.uid.toString())) {
                        viewModel.classList.add(clazz)
                    }
                    updateUI()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })



        addClassroom.setOnClickListener{

            val factory = LayoutInflater.from(context)
            val createClassDialogView: View = factory.inflate(R.layout.dialog_create_class, null)
            val createClassDialog: AlertDialog = AlertDialog.Builder(context).create()
            createClassDialog.setView(createClassDialogView)
            createClassDialogView.findViewById<View>(R.id.submit_button)
                .setOnClickListener{
                    Log.d("ok", "okok")
                    createClassDialog.dismiss()
                }



            createClassDialog.show()



//            Navigation.createNavigateOnClickListener(R.id.action_teacherHomeFragment_to_teacherClassDialog)
//            view.findNavController()
//                .navigate(R.id.action_teacherHomeFragment_to_teacherClassDialog)

        }
        updateUI()
        return view
    }

    private fun updateUI(){
        val classes = viewModel.classList
        adapter = ClassAdapter(classes)
        teacherClassList.adapter = adapter
    }
    private inner class ClassHolder(view:View)
        :RecyclerView.ViewHolder(view),View.OnClickListener{
        private lateinit var c1: Class
        private val cardView:CardView = itemView.findViewById(R.id.list_item_card)
        private val classNameTextView:TextView = itemView.findViewById(R.id.class_name)


        init{
            cardView.setOnClickListener(this)
        }

        fun bind(c1:Class){
            this.c1 = c1
            classNameTextView.text = this.c1.name

        }

        //RECYCLER ITEM KEY PRESS
        override fun onClick(v: View?) {
            viewModel.currentClass = this.c1.cUID
            viewModel.userList.clear()
            Navigation.createNavigateOnClickListener(R.id.action_teacherHomeFragment_to_teacherClassFragment)
            view?.findNavController()?.navigate(R.id.action_teacherHomeFragment_to_teacherClassFragment)
        }
    }
    private inner class ClassAdapter(var classes:List<Class>)
        :RecyclerView.Adapter<ClassHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassHolder {
            val view = layoutInflater.inflate(R.layout.list_item_classrooms_card,parent,false)
            return ClassHolder(view)
        }

        override fun getItemCount() = classes.size


        override fun onBindViewHolder(holder: ClassHolder, position: Int) {
            val c1 = classes[position]
            holder.bind(c1)
        }

    }
}
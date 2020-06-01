package com.example.areyuhere

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')


class UserViewModel : ViewModel() {

    var code = "test"
    var id = ""
    lateinit var currentEmail:String
    var children_count:Long = 0
    //database instance for firebase
    val database = Firebase.database
    val code_ref = database.getReference("code")
    val teacherListRef = database.getReference().child("teacherlist")
    val studentListRef = database.getReference().child("studentlist")
    val studentRef = database.getReference("studentlist")
    val classListRef = database.getReference().child("classlist")
    val classRef = database.getReference("classlist")
    val userList = mutableListOf<User>()
   
    fun newCode() {
        code = (1..8)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")


        code_ref.setValue(code)
    }
    fun resetStatus(){
        for(count in 1 until children_count+1){
            studentRef.child(count.toString()).child("status").setValue("F")
        }
    }

    fun newUID():String{
        code = (1..12)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")


        return code
    }


}



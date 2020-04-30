package com.example.areyuhere

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
private const val TAG = "ViewModel"

class UserViewModel : ViewModel() {

    var code = "test"
    var id = ""
    lateinit var currentEmail:String
    var children_count:Long = 0
    //database instance for firebase
    val database = Firebase.database
    val code_ref = database.getReference("code")
    val isTeacher = database.getReference("teacher")
    val getStatus = database.getReference().child("userlist")
    val setStatus = database.getReference("userlist")
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
            setStatus.child(count.toString()).child("status").setValue("F")
        }
    }


//    fun getC(): String? {
//        var value = ""
//
//
//    }




}



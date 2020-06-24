package com.example.areyuhere

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class UserViewModel : ViewModel() {
    var code = "test"
    var id = ""
    var currentClass = ""
    lateinit var currentEmail:String
    var childrenCount:Long = 0
    val userList = mutableListOf<User>()
    val classList = mutableListOf<Class>()

    //database instance for firebase and references to particular nodes within the db
    val database = Firebase.database
    val teacherListRef = database.reference.child("teacherlist")
    val studentRef = database.getReference("studentlist")
    val studentListRef = database.reference.child("studentlist")
    val classRef = database.getReference("classlist")
    val classListRef = database.reference.child("classlist")

    /*
    * purpose: generates a new 8 character alphanumeric code to be used for checking a student into class
    * inputs: none
    * outputs: code updated in Firebase Realtime DB
    */
    fun newCode(): String {
        code = (1..8)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        return code
    }

    /*
    * purpose: resets everyone in class's checked-in status to false
    * inputs: none
    * outputs: updates checked-in status for each student in Firebase Realtime DB
    */
    fun resetStatus(){
        for(count in 0 until childrenCount){
            classListRef.child(currentClass).child("enrolled students").child(userList[count.toInt()].id).child("status").setValue("F")
        }
    }

    /*
    * purpose: generates 12 character alphanumeric code to be used to uniquely identify a class. Students will join classes using this code.
    * inputs: none
    * outputs: a string that contains the code
    * TODO: 12 character alphanumeric codes are cool, but maybe something more secure or easier for students to type in would be nice.
    */
    fun newUID():String{
        code = (1..12)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        return code
    }

}
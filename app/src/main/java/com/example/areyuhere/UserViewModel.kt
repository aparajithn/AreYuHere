package com.example.areyuhere

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class UserViewModel : ViewModel() {
    var code = "test"
    var id = ""
    lateinit var currentEmail:String
    var childrenCount:Long = 0
    val userList = mutableListOf<User>()

    //database instance for firebase and references to particular nodes within the db
    val database = Firebase.database
    val codeRef = database.getReference("code")
    val teacherListRef = database.reference.child("teacherlist")
    val studentRef = database.getReference("studentlist")
    val studentListRef = database.reference.child("studentlist")
    val classRef = database.getReference("classlist")
    val classListRef = database.reference.child("classlist")

    /*
    * purpose: generates a new 8 character alphanumeric code to be used for checking a student into class
    * inputs: none
    * outputs: code updated in Firebase Realtime DB
    * TODO: currently codeRef is one location, this code will need to be sent to each teacher's particular classroom.
    *  Probably just return the code to teacher fragment and deal with placing the code in the correct location there.
    */
    fun newCode() {
        code = (1..8)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        codeRef.setValue(code)
    }

    /*
    * purpose: resets everyone in class's checked-in status to false
    * inputs: none
    * outputs: updates checked-in status for each student in Firebase Realtime DB
    * TODO: this is how it worked with the one-classroom setup we had during 475 course. Now that scope of project has increased,
    *  we will need to use a reference to the class, get into student list and reset through there. Will need an input of the Ref
    *  for this class
    */
    fun resetStatus(){
        for(count in 1 until childrenCount+1){
            studentRef.child(count.toString()).child("status").setValue("F")
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
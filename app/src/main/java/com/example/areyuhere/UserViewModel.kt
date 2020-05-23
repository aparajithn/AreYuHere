package com.example.areyuhere

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.FileWriter
import java.io.IOException

private val CSV_HEADER = "id,name,status"
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
    val teacherRef = database.getReference("teacherlist")
    val teacherListRef = database.getReference().child("teacherlist")
    val getStatus = database.getReference().child("studentlist")
    val setStatus = database.getReference("studentlist")
    val userList = mutableListOf<User>()
    val teacherList = mutableListOf<Teacher>()
   
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

//    fun export() {
//        var fileWriter: FileWriter? = null
//
//        try {
//            fileWriter = FileWriter("customer.csv")
//
//            fileWriter.append(CSV_HEADER)
//            fileWriter.append('\n')
//
//            for (users in userList) {
//                fileWriter.append(users.id)
//                fileWriter.append(',')
//                fileWriter.append(users.name)
//                fileWriter.append(',')
//                fileWriter.append(users.isCheckedin)
//                fileWriter.append('\n')
//            }
//
//            println("Write CSV successfully!")
//        } catch (e: Exception) {
//            println("Writing CSV error!")
//            e.printStackTrace()
//        } finally {
//            try {
//                fileWriter!!.flush()
//                fileWriter.close()
//            } catch (e: IOException) {
//                println("Flushing/closing error!")
//                e.printStackTrace()
//            }
//        }
//    }
}



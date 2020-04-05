package com.example.areyuhere

import androidx.lifecycle.ViewModel
private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class UserViewModel : ViewModel() {
    //status true means teacher
    var status = false
    var status1 = true
    var userName = "test_student"
    var userName1 = "test_teacher"
    var password = "abc123"
    var password1 = "123abc"
    var code = "test"

    fun getStat(): Boolean {
    return status
    }

    fun getStat1(): Boolean {
        return status1
    }

    fun getUsername(): String {
        return userName
    }

    fun getPass(): String {
        return password
    }

    fun getUsername1(): String {
        return userName1
    }

    fun getPass1(): String {
        return password1
    }

    fun newCode() {
        code = (1..8)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
    fun getC():String{
        return code
    }



}



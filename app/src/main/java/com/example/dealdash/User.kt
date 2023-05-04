package com.example.dealdash

data class User (var firstName: String = "", var lastName: String = "", var mobileNumber: String = "", var facebook: String? = "", var line: String? = ""): java.io.Serializable

data class UserAccount (val firstName: String, val lastName: String, val mobileNumber: String, val facebook: String?, val line: String?, val email: String, val uid: String): java.io.Serializable

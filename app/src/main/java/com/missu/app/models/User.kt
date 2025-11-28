package com.missu.app.models

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val username: String = "",
    val fcmToken: String = ""
)
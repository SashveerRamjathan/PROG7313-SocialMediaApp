package com.fakebook.SocialMediaApp.models

data class User(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val bio: String = "",
    val profilePictureLink: String = "",
    var score: Double = 0.0
)

package com.fakebook.SocialMediaApp.DataModels

data class User(
    val userId: String,
    val email: String,
    val username: String,
    val fullName: String,
    val bio: String,
    val profilePictureLink: String
)

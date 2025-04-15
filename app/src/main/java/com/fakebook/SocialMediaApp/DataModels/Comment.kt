package com.fakebook.SocialMediaApp.DataModels

import com.google.firebase.Timestamp

data class Comment(
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val commentText: String = "",
    val username: String = ""
)

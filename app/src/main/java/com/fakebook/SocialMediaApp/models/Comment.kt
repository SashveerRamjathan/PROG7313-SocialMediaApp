package com.fakebook.SocialMediaApp.models

import com.google.firebase.Timestamp

data class Comment(
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val commentText: String = "",
    val username: String = ""
)

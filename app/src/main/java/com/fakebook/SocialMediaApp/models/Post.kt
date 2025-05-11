package com.fakebook.SocialMediaApp.models

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val username: String = "",
    val tags: List<String> = emptyList()
)

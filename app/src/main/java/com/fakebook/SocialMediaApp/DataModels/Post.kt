package com.fakebook.SocialMediaApp.DataModels

import com.google.firebase.Timestamp

data class Post(
    val postId: String,
    val userId: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Timestamp
)

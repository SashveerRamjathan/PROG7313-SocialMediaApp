package com.fakebook.SocialMediaApp.helpers

import com.fakebook.SocialMediaApp.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object EngagementUtils {

    suspend fun attachEngagementCountsToPosts(posts: List<Post>): List<Post> {
        val db = FirebaseFirestore.getInstance()

        return posts.map { post ->
            val likesRef = db.collection("posts").document(post.postId).collection("likes")
            val commentsRef = db.collection("posts").document(post.postId).collection("comments")

            val likesCount = likesRef.get().await().size()
            val commentsCount = commentsRef.get().await().size()

            post.likes = likesCount
            post.comments = commentsCount

            post
        }
    }

}
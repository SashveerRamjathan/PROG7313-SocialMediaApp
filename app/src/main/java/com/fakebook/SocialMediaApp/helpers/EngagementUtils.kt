package com.fakebook.SocialMediaApp.helpers

import com.fakebook.SocialMediaApp.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * `EngagementUtils` provides helper methods to enrich post data
 * with real-time engagement metrics from Firebase Firestore.
 *
 * ## Responsibilities:
 * - Retrieve and attach like and comment counts to each post.
 * - Use Firebase Firestore collections asynchronously via Kotlin coroutines.
 *
 * This object is stateless and contains only suspendable utility functions.
 */
object EngagementUtils {

    /**
     * Attaches live engagement counts (likes and comments) from Firestore
     * to each [Post] in the provided list. This enriches the post data for
     * ranking or display purposes.
     *
     * Firestore structure assumed:
     * - Collection: "posts"
     *   - Document: postId
     *     - Subcollections: "likes", "comments"
     *
     * @param posts The list of posts to enrich with engagement data.
     * @return A list of [Post] objects with updated likes and comments counts.
     *
     */
    suspend fun attachEngagementCountsToPosts(posts: List<Post>): List<Post> {
        val db = FirebaseFirestore.getInstance()

        return posts.map { post ->
            // Reference to the "likes" and "comments" sub-collections of the post
            val likesRef = db.collection("posts").document(post.postId).collection("likes")
            val commentsRef = db.collection("posts").document(post.postId).collection("comments")

            // Asynchronously fetch the number of likes and comments
            val likesCount = likesRef.get().await().size()
            val commentsCount = commentsRef.get().await().size()

            // Assign the retrieved engagement data to the post
            post.likes = likesCount
            post.comments = commentsCount

            post
        }
    }
}

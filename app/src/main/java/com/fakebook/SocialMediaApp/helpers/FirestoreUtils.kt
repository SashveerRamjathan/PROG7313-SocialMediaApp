package com.fakebook.SocialMediaApp.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.fakebook.SocialMediaApp.models.Comment
import com.fakebook.SocialMediaApp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * `FirestoreUtils` is a utility object that provides helper functions
 * to interact with Firebase Firestore for social media features.
 *
 * This includes:
 * - Counting likes and comments for a post
 * - Retrieving comments for a post
 * - Fetching user profile information
 * - Retrieving the current user's unique identifier (UID)
 *
 * All functions are asynchronous and return results through callbacks.
 */
object FirestoreUtils {

    /**
     * Counts the number of likes on a specific post.
     *
     * @param postId The unique ID of the post.
     * @param callback A lambda function that receives the number of likes.
     */
    fun countLikes(postId: String, callback: (Int) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .collection("likes")
            .count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { aggregateSnapshot ->
                val numLikes = aggregateSnapshot.count
                callback(numLikes.toInt())
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreUtils", "Error counting likes: ", exception)
                callback(0)
            }
    }

    /**
     * Counts the number of comments on a specific post.
     *
     * @param postId The unique ID of the post.
     * @param callback A lambda function that receives the number of comments.
     */
    fun countComments(postId: String, callback: (Int) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .collection("comments")
            .count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { aggregateSnapshot ->
                val numComments = aggregateSnapshot.count
                callback(numComments.toInt())
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreUtils", "Error counting comments: ", exception)
                callback(0)
            }
    }

    /**
     * Retrieves all comments for a specific post, ordered by timestamp.
     *
     * @param postId The unique ID of the post.
     * @param context The Android context used to show a toast message on failure.
     * @param callback A lambda function that receives a list of comments.
     */
    fun getComments(postId: String, context: Context, callback: (List<Comment>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Convert documents to Comment objects
                val comments = querySnapshot.documents.mapNotNull { it.toObject(Comment::class.java) }

                // If no comments exist, return a placeholder message
                if (comments.isEmpty()) {
                    callback(
                        listOf(
                            Comment(
                                userId = "",
                                commentText = "",
                                username = "No Comments, be the first to add one"
                            )
                        )
                    )
                    return@addOnSuccessListener
                }

                callback(comments)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreUtils", "Error loading comments", exception)
                Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            }
    }

    /**
     * Retrieves the profile information of the currently logged-in user.
     *
     * @param userId The unique ID of the user.
     * @param callback A lambda function that receives the user's profile, or null if not found.
     */
    fun getCurrentUserProfile(userId: String, callback: (User?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreUtils", "Error loading user profile", exception)
                callback(null)
            }
    }

    /**
     * Retrieves the unique identifier (UID) of the currently authenticated Firebase user.
     *
     * @return The UID as a [String] if the user is signed in; `null` if no user is authenticated.
     *
     * @see com.google.firebase.auth.FirebaseAuth
     */
    fun getCurrentUserId(): String? {
        // Get the current user from Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Return the UID if the user is authenticated; otherwise, return null
        return currentUser?.uid
    }

}

package com.fakebook.SocialMediaApp.adapters

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.R
import com.fakebook.SocialMediaApp.helpers.FirestoreUtils
import com.fakebook.SocialMediaApp.models.Comment
import com.fakebook.SocialMediaApp.models.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private var posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Firebase instances (you can also pass these from your Activity if needed)
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirestoreUtils.getCurrentUserId() ?: ""

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvNumLikes: TextView = itemView.findViewById(R.id.tvNumLikes)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        val tvNumComments: TextView = itemView.findViewById(R.id.tvNumComments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Load post image using Picasso
        Picasso.get()
            .load(post.imageUrl)
            .placeholder(R.drawable.ic_default_image)
            .into(holder.ivPostImage)

        holder.tvCaption.text = post.caption

        // Format and set timestamp
        val date = post.timestamp.toDate()
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        holder.tvTimestamp.text = sdf.format(date)

        holder.tvUsername.text = post.username

        FirestoreUtils.countLikes(post.postId) { numLikes ->
            holder.tvNumLikes.text = numLikes.toString()
        }

        FirestoreUtils.countComments(post.postId) { numComments ->
            holder.tvNumComments.text = numComments.toString()
        }


        // Reference to the like document for the current user in this post's like sub-collection
        val likeRef = firestore.collection("posts")
            .document(post.postId)
            .collection("likes")
            .document(currentUserId)

        // Check if post is liked by current user
        likeRef.get().addOnSuccessListener { document ->

            if (document.exists())
            {
                // If exists, show the filled like icon
                holder.btnLike.setImageResource(R.drawable.ic_like_filled)
            }
            else
            {
                // Show the unfilled like icon if not liked
                holder.btnLike.setImageResource(R.drawable.ic_like_unfilled)
            }
        }
            .addOnFailureListener {

            // In case of error, ensure a default icon is shown
            holder.btnLike.setImageResource(R.drawable.ic_like_unfilled)
        }

        // Set click listener for the like button
        holder.btnLike.setOnClickListener {

            // Check again for current like status
            likeRef.get().addOnSuccessListener { document ->

                if (document.exists())
                {
                    // User has already liked the post; remove the like
                    likeRef.delete().addOnSuccessListener {

                        // Update the icon once successfully removed
                        holder.btnLike.setImageResource(R.drawable.ic_like_unfilled)

                        // reset the like count
                        FirestoreUtils.countLikes(post.postId) { numLikes ->
                            holder.tvNumLikes.text = numLikes.toString()
                        }
                    }
                }
                else
                {
                    // User hasn't liked the post; add a like entry
                    val likeData = hashMapOf(
                        "userId" to currentUserId,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    likeRef.set(likeData).addOnSuccessListener {
                        // Update the icon once successfully added
                        holder.btnLike.setImageResource(R.drawable.ic_like_filled)

                        // reset the like count
                        FirestoreUtils.countLikes(post.postId) { numLikes ->
                            holder.tvNumLikes.text = numLikes.toString()
                        }
                    }
                }
            }
        }

        // set on click listener for the comment button
        holder.btnComment.setOnClickListener {

            // create a context reference
            val context = holder.itemView.context

            // Inflate custom dialog layout
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null)

            // build the dialog
            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            // Set transparent background if you want rounded corners to show properly
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Show the dialog
            dialog.show()

            // access views from dialog
            val etComment = dialogView.findViewById<EditText>(R.id.etComment)
            val btnPostComment = dialogView.findViewById<Button>(R.id.btnPostComment)
            val rvComments = dialogView.findViewById<RecyclerView>(R.id.rvComments)

            // get comments for this post and set up the adapter for the recycler view
            FirestoreUtils.getComments(post.postId, context) { commentsList ->
                val adapter = CommentAdapter(commentsList.toMutableList())
                rvComments.adapter = adapter
                rvComments.layoutManager = LinearLayoutManager(context)
            }

            // Reference to the comment sub-collection for this post
            val commentsRef = firestore.collection("posts")
                .document(post.postId)
                .collection("comments")

            // Handle button click
            btnPostComment.setOnClickListener {

                val commentText = etComment.text.toString().trim()

                if (commentText.isNotEmpty())
                {
                    FirestoreUtils.getCurrentUserProfile(currentUserId) { currentUser ->

                        if (currentUser != null)
                        {
                            val newComment = Comment(
                                userId = currentUserId,
                                commentText = commentText,
                                username = currentUser.username
                            )

                            // Add the new comment to the Firestore database
                            commentsRef.add(newComment)
                                .addOnSuccessListener {

                                    // log the success
                                    Log.d("PostAdapter", "Comment for post ${post.postId} added with ID: ${it.id}")

                                    // Re-fetch updated comments
                                    FirestoreUtils.getComments(post.postId, context) { updatedComments ->

                                        val newAdapter = CommentAdapter(updatedComments.toMutableList())
                                        rvComments.adapter = newAdapter
                                    }

                                    // reset the comment counter
                                    FirestoreUtils.countComments(post.postId) { numComments ->
                                        holder.tvNumComments.text = numComments.toString()
                                    }

                                    // clear the edit text
                                    etComment.text.clear()

                                }
                                .addOnFailureListener { e ->
                                    // log the error
                                    Log.e("PostAdapter", "Error adding comment for post ${post.postId}", e)

                                    // Dismiss the dialog
                                    dialog.dismiss()

                                    // display the toast error
                                    Toast.makeText(context, "Error adding comment", Toast.LENGTH_SHORT).show()
                                }
                        }
                        else
                        {
                            Log.e("PostAdapter", "Error loading user profile")
                            Toast.makeText(context, "Error loading user profile", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
                }
                else
                {
                    etComment.error = "Comment cannot be empty"
                    etComment.requestFocus()
                }
            }

        }
    }

    override fun getItemCount(): Int = posts.size

    // Update the adapter's data and refresh
    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

}

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
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileAdapter(private var posts: List<Post>) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGridPostImage: ImageView = itemView.findViewById(R.id.ivGridPostImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_post, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val post = posts[position]
        Picasso.get()
            .load(post.imageUrl)
            .placeholder(R.drawable.ic_default_image)
            .into(holder.ivGridPostImage)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_post, null)
            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            // Get views from the dialog layout
            val ivPostImage = dialogView.findViewById<ImageView>(R.id.ivPostImage)
            val tvCaption = dialogView.findViewById<TextView>(R.id.tvCaption)
            val tvTimestamp = dialogView.findViewById<TextView>(R.id.tvTimestamp)
            val tvNumLikes = dialogView.findViewById<TextView>(R.id.tvNumLikes)
            val tvNumComments = dialogView.findViewById<TextView>(R.id.tvNumComments)
            val btnLike = dialogView.findViewById<ImageButton>(R.id.btnLike)
            val btnComment = dialogView.findViewById<ImageButton>(R.id.btnComment)

            // Load post image and data
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.ic_default_image)
                .into(ivPostImage)

            tvCaption.text = post.caption
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = sdf.format(post.timestamp.toDate())

            // Load Likes
            FirestoreUtils.countLikes(post.postId) { numLikes ->
                tvNumLikes.text = numLikes.toString()
            }

            // Load Comments
            FirestoreUtils.countComments(post.postId) { numComments ->
                tvNumComments.text = numComments.toString()
            }

            // disable like button
            btnLike.isEnabled = false

            btnComment.setOnClickListener {

                val currentUserId = FirestoreUtils.getCurrentUserId() ?: ""
                val firestore = FirebaseFirestore.getInstance()

                // Inflate custom dialog layout
                val commentDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null)

                // build the dialog
                val commentDialog = AlertDialog.Builder(context)
                    .setView(commentDialogView)
                    .create()

                // Set transparent background if you want rounded corners to show properly
                commentDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                // Show the dialog
                commentDialog.show()

                // access views from dialog
                val etComment = commentDialog.findViewById<EditText>(R.id.etComment)
                val btnPostComment = commentDialog.findViewById<Button>(R.id.btnPostComment)
                val rvComments = commentDialog.findViewById<RecyclerView>(R.id.rvComments)

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
                                        Log.d("ProfileAdapter", "Comment for post ${post.postId} added with ID: ${it.id}")

                                        // Re-fetch updated comments
                                        FirestoreUtils.getComments(post.postId, context) { updatedComments ->

                                            val newAdapter = CommentAdapter(updatedComments.toMutableList())
                                            rvComments.adapter = newAdapter
                                        }

                                        // reset the comment counter
                                        FirestoreUtils.countComments(post.postId) { numComments ->
                                            tvNumComments.text = numComments.toString()
                                        }

                                        // clear the edit text
                                        etComment.text.clear()

                                    }
                                    .addOnFailureListener { e ->
                                        // log the error
                                        Log.e("ProfileAdapter", "Error adding comment for post ${post.postId}", e)

                                        // Dismiss the dialog
                                        dialog.dismiss()

                                        // display the toast error
                                        Toast.makeText(context, "Error adding comment", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            else
                            {
                                Log.e("ProfileAdapter", "Error loading user profile")
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

    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}

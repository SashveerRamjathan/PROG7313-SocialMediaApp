package com.fakebook.SocialMediaApp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.DataModels.Post
import com.fakebook.SocialMediaApp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private var posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Firebase instances (you can also pass these from your Activity if needed)
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvNumLikes: TextView = itemView.findViewById(R.id.tvNumLikes)
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

        // Count the number of likes for this post
        firestore.collection("posts")
            .document(post.postId)
            .collection("likes")
            .count()
            .get(AggregateSource.SERVER)

            .addOnSuccessListener { aggregateSnapshot ->
                val numLikes = aggregateSnapshot.count
                holder.tvNumLikes.text = numLikes.toString()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error counting likes: ", exception)
                holder.tvNumLikes.text = "0"
            }


        // Reference to the like document for the current user in this post's like sub-collection
        val likeRef = firestore.collection("posts")
            .document(post.postId)
            .collection("likes")
            .document(currentUserId)

        // Check if post is liked by current user
        likeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // If exists, show the filled like icon
                holder.btnLike.setImageResource(R.drawable.ic_like_filled)
            } else {
                // Show the unfilled like icon if not liked
                holder.btnLike.setImageResource(R.drawable.ic_like_unfilled)
            }
        }.addOnFailureListener {
            // In case of error, ensure a default icon is shown
            holder.btnLike.setImageResource(R.drawable.ic_like_unfilled)
        }

        // Set click listener for the like button
        holder.btnLike.setOnClickListener {
            // Check again for current like status
            likeRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // User has already liked the post; remove the like
                    likeRef.delete().addOnSuccessListener {
                        // Update the icon once successfully removed
                        holder.btnLike.setImageResource(R.drawable.ic_like_unfilled)
                    }
                } else {
                    // User hasn't liked the post; add a like entry
                    val likeData = hashMapOf(
                        "userId" to currentUserId,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    likeRef.set(likeData).addOnSuccessListener {
                        // Update the icon once successfully added
                        holder.btnLike.setImageResource(R.drawable.ic_like_filled)
                    }
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

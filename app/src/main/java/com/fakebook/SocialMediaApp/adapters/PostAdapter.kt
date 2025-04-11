package com.fakebook.SocialMediaApp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.DataModels.Post
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private var posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        Picasso.get()
            .load(post.imageUrl)
            .placeholder(R.drawable.ic_default_image)
            .into(holder.ivPostImage)

        holder.tvCaption.text = post.caption

        // Format timestamp. Assuming post.timestamp is a com.google.firebase.Timestamp.
        val date = post.timestamp.toDate()
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        holder.tvTimestamp.text = sdf.format(date)

        holder.tvUsername.text = post.username
    }

    override fun getItemCount(): Int = posts.size

    // Update the data and refresh the adapter
    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}

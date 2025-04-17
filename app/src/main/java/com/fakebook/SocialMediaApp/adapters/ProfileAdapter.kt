package com.fakebook.SocialMediaApp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.R
import com.fakebook.SocialMediaApp.models.Post
import com.squareup.picasso.Picasso

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
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}

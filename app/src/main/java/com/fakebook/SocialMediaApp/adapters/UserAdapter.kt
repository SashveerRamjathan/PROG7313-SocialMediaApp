package com.fakebook.SocialMediaApp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.R
import com.fakebook.SocialMediaApp.models.User
import com.squareup.picasso.Picasso

class UserAdapter(
    private val users: List<User>,
    private val clickListener: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)

        fun bind(user: User) {
            txtUsername.text = user.username
            Picasso.get().load(user.profilePictureLink).into(imgAvatar)
            itemView.setOnClickListener { clickListener(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}
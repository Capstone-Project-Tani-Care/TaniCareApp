package com.dicoding.tanicare.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.ItemPostBinding

data class Thread(
    val username: String,
    val timestamp: String,
    val content: String,
    val imageUrl: String?,
    val likeCount: Int,
    val commentCount: Int,
    val profileImage: String?
)

class ThreadAdapter(private var threadList: List<Thread>) : RecyclerView.Adapter<ThreadAdapter.ThreadViewHolder>() {

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val threadContent: TextView = itemView.findViewById(R.id.threadContent)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val likeCount: TextView = itemView.findViewById(R.id.like_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ThreadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val thread = threadList[position]

        // Bind data to the UI components
        holder.username.text = thread.username
        holder.timestamp.text = thread.timestamp
        holder.threadContent.text = thread.content
        holder.likeCount.text = thread.likeCount.toString()
        holder.commentCount.text = thread.commentCount.toString()

        // Load image for the profile picture
        Glide.with(holder.itemView.context)
            .load(thread.profileImage)
            .placeholder(R.drawable.ic_profile_placeholder) // Placeholder image
            .error(R.drawable.ic_profile_placeholder) // Error image
            .into(holder.profileImage)

        // Check if the post has an image, and if so, display it
        if (thread.imageUrl != null) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(thread.imageUrl)
                .placeholder(R.drawable.bg_farm) // Placeholder image for the post
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = threadList.size

    // Update data in the adapter
    fun updateData(newThreadList: List<Thread>) {
        threadList = newThreadList
        notifyDataSetChanged()
    }
}


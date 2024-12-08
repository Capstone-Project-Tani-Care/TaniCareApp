package com.dicoding.tanicare.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    val profileImage: String?,
    val idThread: String
)

class ThreadAdapter(
    private var threadList: List<Thread>,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val listener: ThreadActionListener// Injected via constructor
) : RecyclerView.Adapter<ThreadAdapter.ThreadViewHolder>() {

    interface ThreadActionListener {
        fun onLikeClicked(threadId: String)
        fun onCommentClicked(threadId: String)
        fun onBookmarkClicked(threadId: String)
    }

    inner class ThreadViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(thread: Thread) {
            with(binding) {
                username.text = thread.username
                timestamp.text = thread.timestamp
                threadContent.text = thread.content
                likeCount.text = thread.likeCount.toString()
                commentCount.text = thread.commentCount.toString()

                // Load profile image
                Glide.with(root.context)
                    .load(thread.profileImage)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImage)

                // Load post image if available
                if (thread.imageUrl != null) {
                    postImage.visibility = View.VISIBLE
                    Glide.with(root.context)
                        .load(thread.imageUrl)
                        .placeholder(R.drawable.bg_farm)
                        .error(R.drawable.bg_farm)
                        .into(postImage)
                } else {
                    postImage.visibility = View.GONE
                }

                // Handle like and comment actions
                likeLayout.setOnClickListener {
                    listener.onLikeClicked(thread.idThread)
                }

                commentLayout.setOnClickListener {
                    listener.onCommentClicked(thread.idThread)
                }

                bookmarkLayout.setOnClickListener{
                    listener.onBookmarkClicked(thread.idThread)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThreadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        holder.bind(threadList[position])
    }

    override fun getItemCount(): Int = threadList.size

    fun updateData(newThreadList: List<Thread>) {
        threadList = emptyList()
        threadList = newThreadList
        notifyDataSetChanged()
    }

}



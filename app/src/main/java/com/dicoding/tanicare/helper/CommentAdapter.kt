package com.dicoding.tanicare.helper

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.ItemCommentBinding

class ItemCommentAdapter(private val comments: List<CommentItem>) : RecyclerView.Adapter<ItemCommentAdapter.CommentViewHolder>() {

    // ViewHolder yang akan mengikat data ke item XML
    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentItem) {
            // Bind the CommentItem model ke views
            binding.tvUsername.text = comment.owner.name
            binding.tvComment.text = comment.content
            Log.d("Profile", "Profile URL: ${comment.owner.photoProfileUrl}")
            // Gunakan gambar profil placeholder untuk sekarang
            Glide.with(binding.imgProfile.context)
                .load(comment.owner.photoProfileUrl)  // Ganti dengan URL gambar profil yang valid
                .placeholder(R.drawable.ic_profile_placeholder)  // Placeholder gambar sementara
                .error(R.drawable.ic_profile_placeholder)  // Gambar error jika gagal memuat
                .circleCrop()  // Opsional: untuk memotong gambar menjadi bentuk lingkaran
                .into(binding.imgProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size
}


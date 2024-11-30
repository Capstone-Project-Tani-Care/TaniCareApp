package com.dicoding.tanicare.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R

class MyPhotoAdapter(private val photos: List<String>) : RecyclerView.Adapter<MyPhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImage: ImageView = itemView.findViewById(R.id.photo_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // Memuat gambar menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(photos[position]) // URL gambar dari API
            .placeholder(R.drawable.ic_placeholder) // Placeholder saat loading
            .error(R.drawable.ic_error) // Jika gagal memuat
            .into(holder.photoImage)
    }

    override fun getItemCount(): Int = photos.size
}

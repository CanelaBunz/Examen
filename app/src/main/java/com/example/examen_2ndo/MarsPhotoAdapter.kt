package com.example.examen_2ndo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MarsPhotosAdapter(private var photos: List<MarsPhoto>) : RecyclerView.Adapter<MarsPhotosAdapter.MarsPhotoViewHolder>() {

    fun updatePhotos(newPhotos: List<MarsPhoto>) {
        photos = newPhotos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarsPhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mars_photo, parent, false)
        return MarsPhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarsPhotoViewHolder, position: Int) {
        val photo = photos[position]
        holder.bind(photo)
    }

    override fun getItemCount() = photos.size

    class MarsPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        private val cameraTextView: TextView = itemView.findViewById(R.id.cameraTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(photo: MarsPhoto) {
            Glide.with(itemView.context)
                .load(photo.img_src)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(photoImageView)

            cameraTextView.text = photo.camera.full_name
            dateTextView.text = photo.earth_date
        }
    }
}
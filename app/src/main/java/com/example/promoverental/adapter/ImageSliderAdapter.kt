package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.promoverental.R

class ImageSliderAdapter(
    private val imageUrls: List<String>,
    private val onImageClick: (String) -> Unit
) :
    RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivSliderImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val url = imageUrls[position]
        holder.imageView.load(url) {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }
        holder.itemView.setOnClickListener { onImageClick(url) }
    }

    override fun getItemCount(): Int = imageUrls.size
}

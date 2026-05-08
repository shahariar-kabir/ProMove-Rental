package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.R
import com.example.promoverental.model.House

class FeaturedHouseAdapter(
    private var houses: List<House>,
    private val onItemClick: (House) -> Unit
) : RecyclerView.Adapter<FeaturedHouseAdapter.FeaturedHouseViewHolder>() {

    class FeaturedHouseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHouse: ImageView = view.findViewById(R.id.ivHouseImage)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvBedrooms: TextView = view.findViewById(R.id.tvBedrooms)
        val tvBathrooms: TextView = view.findViewById(R.id.tvBathrooms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedHouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house_featured, parent, false)
        return FeaturedHouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedHouseViewHolder, position: Int) {
        val house = houses[position]
        holder.tvTitle.text = house.title
        holder.tvLocation.text = house.location
        holder.tvPrice.text = house.price
        holder.tvBedrooms.text = holder.itemView.context.getString(R.string.bedrooms_count, house.bedrooms)
        holder.tvBathrooms.text = holder.itemView.context.getString(R.string.bathrooms_count, house.bathrooms)
        
        // holder.ivHouse.setImageResource(R.drawable.house_placeholder)

        holder.itemView.setOnClickListener { onItemClick(house) }
    }

    override fun getItemCount() = houses.size
}

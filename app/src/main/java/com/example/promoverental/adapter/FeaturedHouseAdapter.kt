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
        
        // Use logo as placeholder for now
        holder.ivHouse.setImageResource(R.drawable.logo)

        holder.itemView.setOnClickListener { onItemClick(house) }
    }

    override fun getItemCount() = houses.size
}

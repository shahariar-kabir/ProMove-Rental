package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.promoverental.R
import com.example.promoverental.model.House

class HouseAdapter(
    private var houses: List<House>,
    private val onItemClick: (House) -> Unit
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    class HouseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHouse: ImageView = view.findViewById(R.id.ivHouseImage)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvArea: TextView = view.findViewById(R.id.tvArea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house_list, parent, false)
        return HouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]
        val context = holder.itemView.context

        holder.tvTitle.text = house.title
        holder.tvPrice.text = context.getString(R.string.rent_amount, house.price)
        holder.tvArea.text = context.getString(R.string.sq_feet, house.area)
        
        holder.ivHouse.load(house.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }

        holder.itemView.setOnClickListener { onItemClick(house) }
    }

    override fun getItemCount() = houses.size

    fun updateData(newHouses: List<House>) {
        houses = newHouses
        notifyDataSetChanged()
    }
}

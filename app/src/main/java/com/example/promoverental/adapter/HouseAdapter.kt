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
    private val isOwner: Boolean = false,
    private val onEditClick: ((House) -> Unit)? = null,
    private val onDeleteClick: ((House) -> Unit)? = null,
    private val onItemClick: (House) -> Unit
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    class HouseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHouse: ImageView = view.findViewById(R.id.ivHouseImage)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvArea: TextView = view.findViewById(R.id.tvArea)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ownerActions: View = view.findViewById(R.id.ownerActions)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house_list, parent, false)
        return HouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]
        val context = holder.itemView.context

        holder.tvTitle.text = house.title ?: "No Title"
        holder.tvPrice.text = context.getString(R.string.rent_amount, house.price ?: "0")
        holder.tvArea.text = context.getString(R.string.sq_feet, house.area ?: "0")
        
        val firstImageUrl = house.imageUrls.firstOrNull()
        holder.ivHouse.load(firstImageUrl) {
            crossfade(true)
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }

        // Owner functionality
        if (isOwner) {
            holder.tvStatus.visibility = View.VISIBLE
            holder.tvStatus.text = house.status ?: "available"
            holder.tvStatus.setTextColor(
                if (house.status == "available" || house.status.isNullOrEmpty()) context.getColor(R.color.success)
                else context.getColor(R.color.secondary)
            )
            holder.ownerActions.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEditClick?.invoke(house) }
            holder.btnDelete.setOnClickListener { onDeleteClick?.invoke(house) }
        } else {
            holder.tvStatus.visibility = View.GONE
            holder.ownerActions.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(house) }
    }

    override fun getItemCount() = houses.size

    fun updateData(newHouses: List<House>) {
        houses = newHouses
        notifyDataSetChanged()
    }
}

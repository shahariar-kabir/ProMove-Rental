package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.promoverental.R
import com.example.promoverental.model.Booking
import com.google.android.material.chip.Chip

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHouse: ImageView = view.findViewById(R.id.ivHouse)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        val house = booking.house
        
        holder.tvTitle.text = house?.title ?: "Unknown House"
        holder.tvPrice.text = house?.price ?: ""
        holder.chipStatus.text = booking.status.replaceFirstChar { it.uppercase() }

        if (house?.imageUrls?.isNotEmpty() == true) {
            holder.ivHouse.load(house.imageUrls[0]) {
                crossfade(true)
                placeholder(R.drawable.logo)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(booking) }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}

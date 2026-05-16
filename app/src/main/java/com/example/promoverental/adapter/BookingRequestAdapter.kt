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
import com.google.android.material.button.MaterialButton

class BookingRequestAdapter(
    private var requests: List<Booking>,
    private val onAccept: (Booking) -> Unit,
    private val onReject: (Booking) -> Unit
) : RecyclerView.Adapter<BookingRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHouse: ImageView = view.findViewById(R.id.ivHouse)
        val tvHouseTitle: TextView = view.findViewById(R.id.tvHouseTitle)
        val tvRequester: TextView = view.findViewById(R.id.tvRequester)
        val btnAccept: MaterialButton = view.findViewById(R.id.btnAccept)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = requests[position]
        val house = booking.house

        holder.tvHouseTitle.text = house?.title ?: "Unknown"
        holder.tvRequester.text = "Requested by User: ${booking.userId.take(8)}..."

        if (house?.imageUrls?.isNotEmpty() == true) {
            holder.ivHouse.load(house.imageUrls[0]) {
                placeholder(R.drawable.logo)
            }
        }

        holder.btnAccept.setOnClickListener { onAccept(booking) }
        holder.btnReject.setOnClickListener { onReject(booking) }
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newRequests: List<Booking>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}

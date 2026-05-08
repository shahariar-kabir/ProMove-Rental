package com.example.promoverental.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.R
import com.example.promoverental.model.MovingProvider

class MovingProviderAdapter(
    private var providers: List<MovingProvider>,
    private val onItemClick: (MovingProvider) -> Unit
) : RecyclerView.Adapter<MovingProviderAdapter.ProviderViewHolder>() {

    class ProviderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProvider: ImageView = view.findViewById(R.id.ivProviderImage)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_moving_provider, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.tvName.text = provider.name
        holder.tvRating.text = holder.itemView.context.getString(R.string.rating_with_reviews, provider.rating, provider.reviews)
        holder.tvPrice.text = provider.pricePerKm
        
        // holder.ivProvider.setImageResource(R.drawable.provider_placeholder)

        holder.itemView.setOnClickListener { onItemClick(provider) }
    }

    override fun getItemCount() = providers.size
}

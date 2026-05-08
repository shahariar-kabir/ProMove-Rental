package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        val rvFavorites = view.findViewById<RecyclerView>(R.id.rvFavorites)
        rvFavorites.layoutManager = LinearLayoutManager(context)

        val favoriteHouses = listOf(
            House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment."),
            House("3", "Luxury Villa", "Uttara, Dhaka", "$1500/mo", 5, 4, "4000 sqft", "Huge villa with a pool.")
        )

        rvFavorites.adapter = HouseAdapter(favoriteHouses) { house ->
            val intent = Intent(context, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }

        return view
    }
}

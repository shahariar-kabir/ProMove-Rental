package com.example.ptomoverental

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptomoverental.adapter.HouseAdapter
import com.example.ptomoverental.model.House
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var houseAdapter: HouseAdapter

    private val sampleHouses = listOf(
        House("1", "2 Bed room", "Gulshan, Dhaka", "5000 BDT/ Month", 2, 1, "1400 Sq feet", "Spacious 2 bedroom apartment.", latitude = 23.7925, longitude = 90.4078),
        House("2", "2 Bed room", "Banani, Dhaka", "5000 BDT/ Month", 2, 1, "1400 Sq feet", "Modern studio near the park.", latitude = 23.7940, longitude = 90.4043),
        House("3", "2 Bed room", "Uttara, Dhaka", "5000 BDT/ Month", 2, 2, "1400 Sq feet", "Luxury villa with garden.", latitude = 23.8759, longitude = 90.3795),
        House("4", "2 Bed room", "Mirpur, Dhaka", "5000 BDT/ Month", 2, 1, "1400 Sq feet", "Affordable flat for small family.", latitude = 23.8041, longitude = 90.3625)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Map
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize RecyclerView
        val rvTopRentals = view.findViewById<RecyclerView>(R.id.rvTopRentals)
        rvTopRentals.layoutManager = LinearLayoutManager(context)
        
        houseAdapter = HouseAdapter(sampleHouses) { house ->
            // When a house is clicked, update map camera as requested
            updateMapLocation(house)
        }
        rvTopRentals.adapter = houseAdapter

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Add markers for all houses
        sampleHouses.forEach { house ->
            val location = LatLng(house.latitude, house.longitude)
            googleMap?.addMarker(MarkerOptions()
                .position(location)
                .title(house.title)
                .snippet(house.location))
        }

        // Move camera to the first house by default (Dhaka area)
        if (sampleHouses.isNotEmpty()) {
            val firstHouse = LatLng(sampleHouses[0].latitude, sampleHouses[0].longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(firstHouse, 12f))
        }
    }

    private fun updateMapLocation(house: House) {
        val location = LatLng(house.latitude, house.longitude)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        
        // Find and show the marker for this house
        googleMap?.addMarker(MarkerOptions()
            .position(location)
            .title(house.title)
            .snippet(house.price))?.showInfoWindow()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
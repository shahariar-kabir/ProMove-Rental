package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.FeaturedHouseAdapter
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var houseAdapter: HouseAdapter

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
        
        houseAdapter = HouseAdapter(emptyList()) { house ->
            val intent = Intent(context, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }
        rvTopRentals.adapter = houseAdapter

        // Initialize Featured Houses
        val rvFeaturedHouses = view.findViewById<RecyclerView>(R.id.rvFeaturedHouses)
        rvFeaturedHouses.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        // Fetch Data from Supabase
        fetchHouses()

        // Buttons
        view.findViewById<MaterialButton>(R.id.btnShifting).setOnClickListener {
            startActivity(Intent(context, MovingServiceActivity::class.java))
        }

        return view
    }

    private fun fetchHouses() {
        lifecycleScope.launch {
            try {
                // select * from houses
                val houses = SupabaseManager.client.postgrest["houses"]
                    .select().decodeList<House>()
                
                houseAdapter.updateData(houses)
                
                // Update Featured Houses (shuffled)
                view?.findViewById<RecyclerView>(R.id.rvFeaturedHouses)?.adapter = 
                    FeaturedHouseAdapter(houses.shuffled()) { house ->
                        val intent = Intent(context, HouseDetailsActivity::class.java)
                        intent.putExtra("house", house)
                        startActivity(intent)
                    }

                // Add markers to map
                houses.forEach { house ->
                    val location = LatLng(house.latitude, house.longitude)
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(house.title)
                            .snippet(house.price)
                    )
                }
            } catch (e: Exception) {
                // Fallback to sample data if database table is not ready
                loadSampleData()
            }
        }
    }

    private fun loadSampleData() {
        val sampleHouses = listOf(
            House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment.", latitude = 23.7925, longitude = 90.4078),
            House("2", "Cozy Studio", "Banani, Dhaka", "$300/mo", 1, 1, "500 sqft", "Perfect for students.", latitude = 23.7940, longitude = 90.4043)
        )
        houseAdapter.updateData(sampleHouses)
        view?.findViewById<RecyclerView>(R.id.rvFeaturedHouses)?.adapter = FeaturedHouseAdapter(sampleHouses) { _ -> }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val dhaka = LatLng(23.8103, 90.4125)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(dhaka, 11f))
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

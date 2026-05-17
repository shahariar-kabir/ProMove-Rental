package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class SearchFragment : Fragment() {

    private lateinit var houseAdapter: HouseAdapter
    private var allHouses: List<House> = emptyList()
    private var isMapView = false
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var map: MapView
    private lateinit var btnToggleView: MaterialButton
    private lateinit var btnFullScreenMap: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        rvSearchResults = view.findViewById(R.id.rvSearchResults)
        map = view.findViewById(R.id.mapview)
        btnToggleView = view.findViewById(R.id.btnToggleView)
        btnFullScreenMap = view.findViewById(R.id.btnFullScreenMap)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        rvSearchResults.layoutManager = LinearLayoutManager(context)
        houseAdapter = HouseAdapter(
            houses = emptyList(),
            onItemClick = { house ->
                val intent = Intent(context, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
        )
        rvSearchResults.adapter = houseAdapter

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(12.0)
        map.controller.setCenter(GeoPoint(23.8103, 90.4125))

        btnToggleView.setOnClickListener { toggleView() }

        btnFullScreenMap.setOnClickListener {
            val intent = Intent(context, FullScreenMapActivity::class.java)
            intent.putExtra("mode", "view_all")
            startActivity(intent)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchHouses()
        return view
    }

    private fun fetchHouses() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch all houses first to ensure we see everything
                val response = SupabaseManager.client.postgrest["houses"]
                    .select().decodeList<House>()
                
                // Filter locally: show available or those with no status set yet (old data)
                allHouses = response.filter { it.status == "available" || it.status.isNullOrEmpty() }
                
                houseAdapter.updateData(allHouses)
                updateMapMarkers(allHouses)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filter(text: String) {
        val filteredList = allHouses.filter { 
            (it.title?.contains(text, ignoreCase = true) == true) || 
            (it.location?.contains(text, ignoreCase = true) == true)
        }
        houseAdapter.updateData(filteredList)
        updateMapMarkers(filteredList)
    }

    private fun toggleView() {
        isMapView = !isMapView
        if (isMapView) {
            rvSearchResults.visibility = View.GONE
            map.visibility = View.VISIBLE
            btnFullScreenMap.visibility = View.VISIBLE
            btnToggleView.setIconResource(R.drawable.ic_search)
        } else {
            rvSearchResults.visibility = View.VISIBLE
            map.visibility = View.GONE
            btnFullScreenMap.visibility = View.GONE
            btnToggleView.setIconResource(R.drawable.ic_location)
        }
    }

    private fun updateMapMarkers(houses: List<House>) {
        map.overlays.clear()
        for (house in houses) {
            val marker = Marker(map)
            marker.position = GeoPoint(house.latitude ?: 23.8103, house.longitude ?: 90.4125)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = house.title ?: "House"
            marker.snippet = house.price ?: ""
            marker.setOnMarkerClickListener { _, _ ->
                val intent = Intent(context, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
                true
            }
            map.overlays.add(marker)
        }
        if (houses.isNotEmpty() && isMapView) {
            val firstHouse = houses.first()
            map.controller.animateTo(GeoPoint(firstHouse.latitude ?: 23.8103, firstHouse.longitude ?: 90.4125))
        }
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        fetchHouses() // Refresh data when returning to fragment
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}

package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.FeaturedHouseAdapter
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.example.promoverental.model.Message
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class HomeFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var locationOverlay: MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        map = view.findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(12.0)
        map.controller.setCenter(GeoPoint(23.8103, 90.4125))

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        val rvTopRentals = view.findViewById<RecyclerView>(R.id.rvTopRentals)
        rvTopRentals.layoutManager = LinearLayoutManager(context)
        
        houseAdapter = HouseAdapter(
            houses = emptyList(),
            onItemClick = { house ->
                val intent = Intent(context, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
        )
        rvTopRentals.adapter = houseAdapter

        fetchHouses()

        view.findViewById<MaterialButton>(R.id.btnShifting).setOnClickListener {
            startActivity(Intent(context, MovingServiceActivity::class.java))
        }

        view.findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(context, NotificationActivity::class.java))
        }

        view.findViewById<View>(R.id.btnMessagesContainer).setOnClickListener {
            startActivity(Intent(context, InboxActivity::class.java))
        }

        view.findViewById<View>(R.id.searchBar).setOnClickListener {
            (activity as? MainActivity)?.findViewById<View>(R.id.nav_search)?.performClick()
        }

        view.findViewById<View>(R.id.btnFullScreenMap).setOnClickListener {
            val intent = Intent(context, FullScreenMapActivity::class.java)
            intent.putExtra("mode", "view_all")
            startActivity(intent)
        }

        view.findViewById<View>(R.id.tvViewAllFeatured).setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_search
        }

        view.findViewById<View>(R.id.tvViewAllTop).setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_search
        }

        return view
    }

    private fun fetchHouses() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = SupabaseManager.client.postgrest["houses"]
                    .select().decodeList<House>()
                
                val availableHouses = response.filter { it.status == "available" || it.status.isNullOrEmpty() }
                
                houseAdapter.updateData(availableHouses)
                
                val rvFeatured = view?.findViewById<RecyclerView>(R.id.rvFeaturedHouses)
                if (rvFeatured != null && isAdded) {
                    rvFeatured.adapter = FeaturedHouseAdapter(availableHouses.shuffled()) { house ->
                        val intent = Intent(context, HouseDetailsActivity::class.java)
                        intent.putExtra("house", house)
                        startActivity(intent)
                    }
                }

                updateMapMarkers(availableHouses)

            } catch (e: Exception) {
                loadSampleData()
            }
        }
    }

    private fun updateMapMarkers(houses: List<House>) {
        map.overlays.clear()
        map.overlays.add(locationOverlay)
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
        map.invalidate()
    }

    private fun loadSampleData() {
        val sampleHouses = listOf(
            House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment.", latitude = 23.7925, longitude = 90.4078),
            House("2", "Cozy Studio", "Banani, Dhaka", "$300/mo", 1, 1, "500 sqft", "Perfect for students.", latitude = 23.7940, longitude = 90.4043)
        )
        houseAdapter.updateData(sampleHouses)
        view?.findViewById<RecyclerView>(R.id.rvFeaturedHouses)?.adapter = FeaturedHouseAdapter(sampleHouses) { _ -> }
        updateMapMarkers(sampleHouses)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        locationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                if (isAdded) {
                    map.controller.animateTo(locationOverlay.myLocation)
                    map.controller.setZoom(15.0)
                    map.invalidate()
                }
            }
        }
        fetchHouses()
        loadUnreadMessageCount()
    }

    private fun loadUnreadMessageCount() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        val tvBadge = view?.findViewById<TextView>(R.id.tvMessageBadge) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val count = SupabaseManager.client.postgrest["messages"]
                    .select {
                        filter {
                            eq("receiver_id", userId)
                            eq("is_read", false)
                        }
                    }.decodeList<Message>().size

                if (count > 0) {
                    tvBadge.visibility = View.VISIBLE
                    tvBadge.text = if (count > 99) "99+" else count.toString()
                } else {
                    tvBadge.visibility = View.GONE
                }
            } catch (e: Exception) { }
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        locationOverlay.disableMyLocation()
    }
}

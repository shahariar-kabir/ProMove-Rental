package com.example.promoverental

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class FullScreenMapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var mode: String = "view" // view_single, view_all, pick
    private var selectedHouse: House? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_full_screen_map)

        mode = intent.getStringExtra("mode") ?: "view_all"
        selectedHouse = intent.getSerializableExtra("house") as? House

        map = findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(12.0)

        // My Location
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        findViewById<View>(R.id.btnClose).setOnClickListener { finish() }
        findViewById<View>(R.id.fabMyLocation).setOnClickListener {
            if (mode == "view_single" && selectedHouse != null) {
                val p = GeoPoint(selectedHouse?.latitude ?: 23.8103, selectedHouse?.longitude ?: 90.4125)
                map.controller.animateTo(p)
                map.controller.setZoom(18.0)
            } else {
                locationOverlay.myLocation?.let {
                    map.controller.animateTo(it)
                    map.controller.setZoom(15.0)
                } ?: run {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        when (mode) {
            "view_single" -> {
                selectedHouse?.let {
                    val p = GeoPoint(it.latitude ?: 23.8103, it.longitude ?: 90.4125)
                    addMarker(it)
                    mapController.setCenter(p)
                    mapController.setZoom(18.0)
                }
            }
            "view_all" -> {
                fetchAndShowAllHouses()
            }
            "pick" -> {
                setupPicker()
            }
        }
    }

    private fun addMarker(house: House) {
        val marker = Marker(map)
        marker.position = GeoPoint(house.latitude ?: 23.8103, house.longitude ?: 90.4125)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = house.title ?: "House"
        marker.snippet = house.price ?: ""
        marker.setOnMarkerClickListener { _, _ ->
            if (mode == "view_all") {
                val intent = Intent(this, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
            true
        }
        map.overlays.add(marker)
    }

    private fun fetchAndShowAllHouses() {
        lifecycleScope.launch {
            try {
                val allHouses = SupabaseManager.client.postgrest["houses"]
                    .select().decodeList<House>()
                
                val availableHouses = allHouses.filter { it.status == "available" || it.status.isNullOrEmpty() }
                
                availableHouses.forEach { addMarker(it) }
                if (availableHouses.isNotEmpty()) {
                    val first = availableHouses[0]
                    map.controller.setCenter(GeoPoint(first.latitude ?: 23.8103, first.longitude ?: 90.4125))
                }
                map.invalidate()
            } catch (e: Exception) { }
        }
    }

    private fun setupPicker() {
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                val resultIntent = Intent()
                resultIntent.putExtra("lat", p.latitude)
                resultIntent.putExtra("lng", p.longitude)
                setResult(RESULT_OK, resultIntent)
                finish()
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
        map.overlays.add(MapEventsOverlay(eventsReceiver))
        
        // Initial center
        val startLat = intent.getDoubleExtra("lat", 23.8103)
        val startLng = intent.getDoubleExtra("lng", 90.4125)
        map.controller.setCenter(GeoPoint(startLat, startLng))
        map.controller.setZoom(15.0)
        
        Toast.makeText(this, "Tap to select location", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}

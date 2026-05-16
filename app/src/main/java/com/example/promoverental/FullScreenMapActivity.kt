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
            locationOverlay.myLocation?.let {
                mapController.animateTo(it)
                mapController.setZoom(15.0)
            }
        }

        when (mode) {
            "view_single" -> {
                selectedHouse?.let {
                    val p = GeoPoint(it.latitude, it.longitude)
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
        marker.position = GeoPoint(house.latitude, house.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = house.title
        marker.snippet = house.price
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
                val response = SupabaseManager.client.postgrest["houses"]
                    .select { filter { eq("status", "available") } }
                    .decodeList<House>()
                
                response.forEach { addMarker(it) }
                if (response.isNotEmpty()) {
                    map.controller.setCenter(GeoPoint(response[0].latitude, response[0].longitude))
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

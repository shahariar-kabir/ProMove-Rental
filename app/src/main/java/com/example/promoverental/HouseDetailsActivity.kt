package com.example.promoverental

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.example.promoverental.adapter.ImageSliderAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.color.DynamicColors
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class HouseDetailsActivity : AppCompatActivity() {

    private var isFavorite = false
    private lateinit var btnFavorite: FloatingActionButton
    private var house: House? = null
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // OSM Configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        setContentView(R.layout.activity_house_details)

        house = intent.getSerializableExtra("house") as? House

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Initialize Map
        map = findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        house?.let {
            findViewById<TextView>(R.id.tvTitle).text = it.title
            findViewById<TextView>(R.id.tvLocation).text = it.location
            findViewById<TextView>(R.id.tvPrice).text = it.price
            findViewById<TextView>(R.id.tvBedrooms).text = it.bedrooms.toString()
            findViewById<TextView>(R.id.tvBathrooms).text = it.bathrooms.toString()
            findViewById<TextView>(R.id.tvArea).text = it.area
            findViewById<TextView>(R.id.tvDescription).text = it.description
            
            // Fetch Owner details (Simulated for MVP, but passing what we have)
            val ownerName = "Owner" // In a real app, you'd fetch this from a 'profiles' table
            findViewById<TextView>(R.id.tvOwnerName).text = ownerName
            findViewById<TextView>(R.id.tvOwnerPhone).text = "+880 1234 567890"

            // Setup Image Slider
            val viewPager = findViewById<ViewPager2>(R.id.viewPagerImages)
            val indicatorLayout = findViewById<LinearLayout>(R.id.pageIndicator)
            
            if (it.imageUrls.isNotEmpty()) {
                viewPager.adapter = ImageSliderAdapter(it.imageUrls) { url ->
                    val intent = Intent(this, FullScreenImageActivity::class.java)
                    intent.putExtra("image_url", url)
                    startActivity(intent)
                }
                setupIndicators(it.imageUrls.size, indicatorLayout)
                setCurrentIndicator(0, indicatorLayout)

                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setCurrentIndicator(position, indicatorLayout)
                    }
                })
            }

            // Setup Map Location
            val startPoint = GeoPoint(it.latitude, it.longitude)
            val mapController = map.controller
            mapController.setZoom(17.5)
            mapController.setCenter(startPoint)

            val startMarker = Marker(map)
            startMarker.position = startPoint
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            startMarker.title = it.title
            map.overlays.add(startMarker)

            findViewById<View>(R.id.btnFullScreenMap).setOnClickListener {
                val intent = Intent(this, FullScreenMapActivity::class.java)
                intent.putExtra("mode", "view_single")
                intent.putExtra("house", house)
                startActivity(intent)
            }

            // Check if favorite
            checkFavoriteStatus(it.id ?: "")
        }

        btnFavorite = findViewById(R.id.btnFavorite)

        findViewById<View>(R.id.btnBookNow).setOnClickListener {
            house?.let { h -> bookHouse(h) }
        }

        btnFavorite.setOnClickListener {
            house?.let { h -> toggleFavorite(h) }
        }

        findViewById<View>(R.id.btnCallOwner).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+8801234567890")
            startActivity(intent)
        }

        findViewById<View>(R.id.btnMessageOwner).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiver_id", house?.ownerId)
            intent.putExtra("owner_name", findViewById<TextView>(R.id.tvOwnerName).text.toString())
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    private fun checkFavoriteStatus(houseId: String) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        lifecycleScope.launch {
            try {
                val favorite = SupabaseManager.client.postgrest["favorites"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("house_id", houseId)
                        }
                    }.decodeSingleOrNull<Map<String, String>>()
                
                isFavorite = favorite != null
                updateFavoriteUI()
            } catch (e: Exception) { }
        }
    }

    private fun toggleFavorite(house: House) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        lifecycleScope.launch {
            try {
                if (isFavorite) {
                    SupabaseManager.client.postgrest["favorites"].delete {
                        filter {
                            eq("user_id", userId)
                            eq("house_id", house.id ?: "")
                        }
                    }
                    isFavorite = false
                    Toast.makeText(this@HouseDetailsActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    val data = buildJsonObject {
                        put("user_id", userId)
                        put("house_id", house.id ?: "")
                    }
                    SupabaseManager.client.postgrest["favorites"].insert(data)
                    isFavorite = true
                    Toast.makeText(this@HouseDetailsActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                }
                updateFavoriteUI()
            } catch (e: Exception) {
                Toast.makeText(this@HouseDetailsActivity, "Operation failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteUI() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite)
            btnFavorite.imageTintList = ContextCompat.getColorStateList(this, R.color.error)
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite)
            btnFavorite.imageTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)
        }
    }

    private fun bookHouse(house: House) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        lifecycleScope.launch {
            try {
                val data = buildJsonObject {
                    put("user_id", userId)
                    put("house_id", house.id ?: "")
                    put("status", "pending")
                }
                SupabaseManager.client.postgrest["bookings"].insert(data)
                Toast.makeText(this@HouseDetailsActivity, "Booking request sent to owner!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@HouseDetailsActivity, "Booking failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupIndicators(count: Int, container: LinearLayout) {
        container.removeAllViews()
        val indicators = arrayOfNulls<ImageView>(count)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            val layoutParams = LinearLayout.LayoutParams(24, 24)
            layoutParams.setMargins(8, 0, 8, 0)
            indicators[i]?.apply {
                setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.bg_circle_white))
                this.layoutParams = layoutParams
                alpha = 0.4f
            }
            container.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int, container: LinearLayout) {
        val childCount = container.childCount
        for (i in 0 until childCount) {
            val imageView = container.getChildAt(i) as ImageView
            if (i == index) {
                imageView.animate().alpha(1.0f).scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            } else {
                imageView.animate().alpha(0.4f).scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
            }
        }
    }
}

package com.example.promoverental

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.SelectedImageAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import java.util.UUID

class AddHouseActivity : AppCompatActivity() {

    private val selectedImages = mutableListOf<Uri>()
    private var existingImageUrls = mutableListOf<String>()
    private lateinit var imageAdapter: SelectedImageAdapter
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var btnPublish: MaterialButton
    private lateinit var etLocation: EditText
    private var isEditMode = false
    private var houseToEdit: House? = null
    
    private lateinit var map: MapView
    private var selectedLocation: GeoPoint = GeoPoint(23.8103, 90.4125)
    private var locationMarker: Marker? = null
    private lateinit var geocoder: Geocoder

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            imageAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSM Configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        setContentView(R.layout.activity_add_house)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etPrice = findViewById<EditText>(R.id.etRent)
        etLocation = findViewById<EditText>(R.id.etAddress)
        val etBedrooms = findViewById<EditText>(R.id.etBedrooms)
        val etBathrooms = findViewById<EditText>(R.id.etBathrooms)
        val etArea = findViewById<EditText>(R.id.etArea)
        val switchAvailable = findViewById<MaterialSwitch>(R.id.switchAvailable)
        
        geocoder = Geocoder(this, Locale.getDefault())

        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnPublish = findViewById(R.id.btnPublish)
        val rvImages = findViewById<RecyclerView>(R.id.rvImages)

        // Map Setup
        map = findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(selectedLocation)

        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                updateLocation(p, true)
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
        map.overlays.add(MapEventsOverlay(eventsReceiver))

        // Address Field Search Logic
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilAddress).setEndIconOnClickListener {
            searchLocationFromAddress(etLocation.text.toString())
        }

        etLocation.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                searchLocationFromAddress(v.text.toString())
                true
            } else {
                false
            }
        }

        // Check if we are in Edit Mode
        houseToEdit = intent.getSerializableExtra("house") as? House
        if (houseToEdit != null) {
            isEditMode = true
            btnPublish.text = "Update House"
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Edit House"
            
            // Pre-fill data
            etTitle.setText(houseToEdit?.title ?: "")
            etDescription.setText(houseToEdit?.description ?: "")
            etPrice.setText(houseToEdit?.price ?: "")
            etLocation.setText(houseToEdit?.location ?: "")
            etBedrooms.setText((houseToEdit?.bedrooms ?: 0).toString())
            etBathrooms.setText((houseToEdit?.bathrooms ?: 0).toString())
            etArea.setText(houseToEdit?.area ?: "")
            switchAvailable.isChecked = (houseToEdit?.status ?: "available") == "available"
            existingImageUrls = houseToEdit?.imageUrls?.toMutableList() ?: mutableListOf()
            
            updateLocation(GeoPoint(houseToEdit?.latitude ?: 23.8103, houseToEdit?.longitude ?: 90.4125), false)
        }

        // Image Preview
        imageAdapter = SelectedImageAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            imageAdapter.notifyDataSetChanged()
        }
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imageAdapter

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        btnAddPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnPublish.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val price = etPrice.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val bedrooms = etBedrooms.text.toString().toIntOrNull() ?: 0
            val bathrooms = etBathrooms.text.toString().toIntOrNull() ?: 0
            val area = etArea.text.toString().trim()
            val status = if (switchAvailable.isChecked) "available" else "rented"

            if (title.isEmpty() || price.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            publishHouse(title, description, price, location, bedrooms, bathrooms, area, status)
        }
    }

    private fun updateLocation(p: GeoPoint, updateTextField: Boolean) {
        selectedLocation = p
        if (locationMarker == null) {
            locationMarker = Marker(map)
            locationMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(locationMarker)
        }
        locationMarker?.position = p
        map.controller.animateTo(p)
        map.invalidate()

        if (updateTextField) {
            reverseGeocode(p)
        }
    }

    private fun reverseGeocode(p: GeoPoint) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    withContext(Dispatchers.Main) {
                        etLocation.setText(address)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun searchLocationFromAddress(addressStr: String) {
        if (addressStr.isEmpty()) return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocationName(addressStr, 1)
                if (!addresses.isNullOrEmpty()) {
                    val lat = addresses[0].latitude
                    val lng = addresses[0].longitude
                    val p = GeoPoint(lat, lng)
                    withContext(Dispatchers.Main) {
                        updateLocation(p, false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddHouseActivity, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddHouseActivity, "Error finding location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun publishHouse(title: String, description: String, price: String, location: String, bedrooms: Int, bathrooms: Int, area: String, status: String) {
        val user = SupabaseManager.client.auth.currentUserOrNull()
        if (user == null) return

        btnPublish.isEnabled = false
        btnPublish.text = if (isEditMode) "Updating..." else "Publishing..."

        lifecycleScope.launch {
            try {
                val newUrls = uploadAllImages()
                val allUrls = existingImageUrls + newUrls

                val house = House(
                    id = houseToEdit?.id ?: UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    price = price,
                    location = location,
                    bedrooms = bedrooms,
                    bathrooms = bathrooms,
                    area = area,
                    imageUrls = allUrls,
                    ownerId = user.id,
                    latitude = selectedLocation.latitude,
                    longitude = selectedLocation.longitude,
                    status = status
                )

                if (isEditMode) {
                    SupabaseManager.client.postgrest["houses"].update(house) {
                        filter { eq("id", house.id ?: "") }
                    }
                } else {
                    SupabaseManager.client.postgrest["houses"].insert(house)
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddHouseActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                btnPublish.isEnabled = true
                btnPublish.text = if (isEditMode) "Update House" else "Publish House"
            }
        }
    }

    private suspend fun uploadAllImages(): List<String> = withContext(Dispatchers.IO) {
        val uploadedUrls = mutableListOf<String>()
        val bucket = SupabaseManager.client.storage["houses"]
        
        selectedImages.forEach { uri ->
            try {
                val fileName = "${UUID.randomUUID()}.jpg"
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@forEach
                inputStream.close()
                bucket.upload(fileName, bytes)
                uploadedUrls.add(bucket.publicUrl(fileName))
            } catch (e: Exception) { }
        }
        return@withContext uploadedUrls
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

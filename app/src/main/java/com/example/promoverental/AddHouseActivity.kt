package com.example.promoverental

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.SelectedImageAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AddHouseActivity : AppCompatActivity() {

    private val selectedImages = mutableListOf<Uri>()
    private var existingImageUrls = mutableListOf<String>()
    private lateinit var imageAdapter: SelectedImageAdapter
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var btnPublish: MaterialButton
    private var isEditMode = false
    private var houseToEdit: House? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            imageAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_house)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etPrice = findViewById<EditText>(R.id.etRent)
        val etLocation = findViewById<EditText>(R.id.etAddress)
        val etBedrooms = findViewById<EditText>(R.id.etBedrooms)
        val etBathrooms = findViewById<EditText>(R.id.etBathrooms)
        val etArea = findViewById<EditText>(R.id.etArea)
        
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnPublish = findViewById(R.id.btnPublish)
        val rvImages = findViewById<RecyclerView>(R.id.rvImages)

        // Check if we are in Edit Mode
        houseToEdit = intent.getSerializableExtra("house") as? House
        if (houseToEdit != null) {
            isEditMode = true
            btnPublish.text = "Update House"
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Edit House"
            
            // Pre-fill data
            etTitle.setText(houseToEdit?.title)
            etDescription.setText(houseToEdit?.description)
            etPrice.setText(houseToEdit?.price)
            etLocation.setText(houseToEdit?.location)
            etBedrooms.setText(houseToEdit?.bedrooms.toString())
            etBathrooms.setText(houseToEdit?.bathrooms.toString())
            etArea.setText(houseToEdit?.area)
            existingImageUrls = houseToEdit?.imageUrls?.toMutableList() ?: mutableListOf()
        }

        // Image Preview (For new selections only in this simple version)
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

            if (title.isEmpty() || price.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isEditMode && selectedImages.isEmpty()) {
                Toast.makeText(this, "Select at least one image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            publishHouse(title, description, price, location, bedrooms, bathrooms, area)
        }
    }

    private fun publishHouse(title: String, description: String, price: String, location: String, bedrooms: Int, bathrooms: Int, area: String) {
        val user = SupabaseManager.client.auth.currentUserOrNull()
        if (user == null) return

        btnPublish.isEnabled = false
        btnPublish.text = if (isEditMode) "Updating..." else "Publishing..."

        lifecycleScope.launch {
            try {
                // 1. Upload new images
                val newUrls = uploadAllImages()
                val allUrls = existingImageUrls + newUrls

                // 2. Prepare Data
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
                    ownerId = user.id
                )

                if (isEditMode) {
                    SupabaseManager.client.postgrest["houses"].update(house) {
                        filter { eq("id", house.id ?: "") }
                    }
                    Toast.makeText(this@AddHouseActivity, "House updated successfully!", Toast.LENGTH_LONG).show()
                } else {
                    SupabaseManager.client.postgrest["houses"].insert(house)
                    Toast.makeText(this@AddHouseActivity, "House published successfully!", Toast.LENGTH_LONG).show()
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
}

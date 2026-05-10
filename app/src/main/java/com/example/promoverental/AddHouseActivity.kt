package com.example.promoverental

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

    private var selectedImageUri: Uri? = null
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var btnPublish: MaterialButton

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            btnAddPhoto.text = "Photo Selected"
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

            if (title.isEmpty() || price.isEmpty() || location.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            publishHouse(title, description, price, location, bedrooms, bathrooms, area)
        }
    }

    private fun publishHouse(title: String, description: String, price: String, location: String, bedrooms: Int, bathrooms: Int, area: String) {
        val user = SupabaseManager.client.auth.currentUserOrNull()
        
        if (user == null) {
            Toast.makeText(this, "You must be logged in to publish", Toast.LENGTH_SHORT).show()
            return
        }

        btnPublish.isEnabled = false
        btnPublish.text = "Publishing..."

        lifecycleScope.launch {
            try {
                // 1. Upload Image
                val imageUrl = uploadImage() ?: throw Exception("Image upload failed")

                // 2. Insert Data
                val house = House(
                    title = title,
                    description = description,
                    price = price,
                    location = location,
                    bedrooms = bedrooms,
                    bathrooms = bathrooms,
                    area = area,
                    imageUrl = imageUrl,
                    ownerId = user.id // নিশ্চিতভাবে ইউজারের আইডি পাঠানো হচ্ছে
                )

                SupabaseManager.client.postgrest["houses"].insert(house)

                Toast.makeText(this@AddHouseActivity, "House published successfully!", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddHouseActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                btnPublish.isEnabled = true
                btnPublish.text = "Publish House"
            }
        }
    }

    private suspend fun uploadImage(): String? = withContext(Dispatchers.IO) {
        val uri = selectedImageUri ?: return@withContext null
        val fileName = "${UUID.randomUUID()}.jpg"
        val bucket = SupabaseManager.client.storage["houses"]
        
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: return@withContext null
        inputStream.close()

        bucket.upload(fileName, bytes)
        return@withContext bucket.publicUrl(fileName)
    }
}

package com.example.promoverental

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.minutes

class EditProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivProfile.load(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        ivProfile = findViewById(R.id.ivProfile)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etOccupation = findViewById<EditText>(R.id.etOccupation)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etBio = findViewById<EditText>(R.id.etBio)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnChangePhoto = findViewById<View>(R.id.btnChangePhoto)

        // Load existing data from Profile Table
        val user = SupabaseManager.client.auth.currentUserOrNull()
        user?.let {
            etEmail.setText(it.email)
            lifecycleScope.launch {
                try {
                    val profile = SupabaseManager.client.postgrest["profiles"]
                        .select { filter { eq("id", user.id) } }
                        .decodeSingleOrNull<com.example.promoverental.model.Profile>()
                    
                    profile?.let { p ->
                        etFullName.setText(p.fullName)
                        etPhone.setText(p.phone)
                        etAddress.setText(p.location)
                        etOccupation.setText(p.occupation)
                        etAge.setText(p.age?.toString())
                        etBio.setText(p.bio)
                        
                        if (!p.avatarUrl.isNullOrEmpty()) {
                            ivProfile.load(p.avatarUrl) {
                                placeholder(R.drawable.logo)
                                error(R.drawable.logo)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to metadata if table fetch fails
                    val metadata = it.userMetadata
                    etFullName.setText(metadata?.get("full_name")?.toString()?.replace("\"", ""))
                    etPhone.setText(metadata?.get("phone")?.toString()?.replace("\"", ""))
                    etAddress.setText(metadata?.get("location")?.toString()?.replace("\"", ""))
                    etBio.setText(metadata?.get("bio")?.toString()?.replace("\"", ""))
                }
            }
        }

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        btnChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val occupation = etOccupation.text.toString().trim()
            val ageStr = etAge.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    var finalAvatarUrl: String? = null
                    
                    // Fetch existing avatar url from profile first
                    val existingProfile = SupabaseManager.client.postgrest["profiles"]
                        .select { filter { eq("id", user?.id ?: "") } }
                        .decodeSingleOrNull<com.example.promoverental.model.Profile>()
                    finalAvatarUrl = existingProfile?.avatarUrl

                    // Upload image if selected
                    selectedImageUri?.let { uri ->
                        val bytes = contentResolver.openInputStream(uri)?.readBytes()
                        if (bytes != null) {
                            val fileName = "avatars/${user?.id}_${System.currentTimeMillis()}.jpg"
                            val bucket = SupabaseManager.client.storage.from("avatars")
                            bucket.upload(fileName, bytes) {
                                upsert = true
                            }
                            finalAvatarUrl = bucket.publicUrl(fileName)
                        }
                    }

                    // Update User Metadata (Auth)
                    SupabaseManager.client.auth.updateUser {
                        data = buildJsonObject {
                            put("full_name", name)
                            put("phone", phone)
                            put("location", address)
                            put("bio", bio)
                            if (finalAvatarUrl != null) {
                                put("avatar_url", finalAvatarUrl!!)
                            }
                        }
                    }

                    // Update Profiles Table
                    val profileData = buildJsonObject {
                        put("id", user?.id)
                        put("full_name", name)
                        put("phone", phone)
                        put("location", address)
                        put("occupation", occupation)
                        if (ageStr.isNotEmpty()) put("age", ageStr.toInt())
                        put("bio", bio)
                        if (finalAvatarUrl != null) {
                            put("avatar_url", finalAvatarUrl)
                        }
                    }
                    SupabaseManager.client.postgrest["profiles"].upsert(profileData)

                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EditProfileActivity, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

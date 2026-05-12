package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ivProfile = findViewById(R.id.ivProfile)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvAddress = findViewById(R.id.tvAddress)

        val user = SupabaseManager.client.auth.currentUserOrNull()
        val currentRole = user?.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "finder"

        findViewById<android.view.View>(R.id.toolbar).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnSwitchRole).setOnClickListener {
            val newRole = if (currentRole == "finder") "owner" else "finder"
            
            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.updateUser {
                        data = buildJsonObject {
                            put("role", newRole)
                        }
                    }
                    
                    Toast.makeText(this@ProfileActivity, "Role switched to $newRole", Toast.LENGTH_SHORT).show()
                    
                    val nextActivity = if (newRole == "owner") OwnerDashboardActivity::class.java else MainActivity::class.java
                    val intent = Intent(this@ProfileActivity, nextActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Switch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            lifecycleScope.launch {
                SupabaseManager.client.auth.signOut()
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = SupabaseManager.client.auth.currentUserOrNull()
        user?.let {
            val metadata = it.userMetadata
            val name = metadata?.get("full_name")?.toString()?.replace("\"", "") ?: "User"
            val phone = metadata?.get("phone")?.toString()?.replace("\"", "") ?: "Not set"
            val address = metadata?.get("location")?.toString()?.replace("\"", "") ?: "Not set"
            val avatarUrl = metadata?.get("avatar_url")?.toString()?.replace("\"", "")

            tvName.text = name
            tvEmail.text = it.email
            tvPhone.text = phone
            tvAddress.text = address

            if (!avatarUrl.isNullOrEmpty()) {
                ivProfile.load(avatarUrl) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
            } else {
                ivProfile.setImageResource(R.drawable.logo)
            }
        }
    }
}

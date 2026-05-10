package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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
}

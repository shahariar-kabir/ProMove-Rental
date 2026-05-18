package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.promoverental.model.Profile
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileFragment : Fragment() {

    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvOccupation: TextView
    private lateinit var tvBio: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        ivProfile = view.findViewById(R.id.ivProfile)
        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvMemberSince = view.findViewById(R.id.tvMemberSince)
        tvPhone = view.findViewById(R.id.tvProfilePhone)
        tvLocation = view.findViewById(R.id.tvProfileLocation)
        tvOccupation = view.findViewById(R.id.tvProfileOccupation)
        tvBio = view.findViewById(R.id.tvProfileBio)

        val user = SupabaseManager.client.auth.currentUserOrNull()
        val currentRole = user?.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "finder"

        view.findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnMyBookings).setOnClickListener {
            startActivity(Intent(context, MyBookingsActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnFavorites).setOnClickListener {
            (activity as? MainActivity)?.findViewById<View>(R.id.nav_favorites)?.performClick()
        }

        view.findViewById<MaterialButton>(R.id.btnSwitchRole).setOnClickListener {
            val newRole = if (currentRole == "finder") "owner" else "finder"
            
            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.updateUser {
                        data = buildJsonObject {
                            put("role", newRole)
                        }
                    }
                    
                    Toast.makeText(context, "Role switched to $newRole", Toast.LENGTH_SHORT).show()
                    
                    val nextActivity = if (newRole == "owner") OwnerDashboardActivity::class.java else MainActivity::class.java
                    val intent = Intent(context, nextActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                } catch (e: Exception) {
                    Toast.makeText(context, "Switch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener {
            Toast.makeText(context, "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            lifecycleScope.launch {
                SupabaseManager.client.auth.signOut()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun loadUserProfile() {
        val user = SupabaseManager.client.auth.currentUserOrNull() ?: return
        tvEmail.text = user.email
        
        val createdAt = user.createdAt?.toString()?.split("T")?.firstOrNull() ?: "2024"
        tvMemberSince.text = "Member since $createdAt"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = SupabaseManager.client.postgrest["profiles"]
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<Profile>()

                profile?.let { p ->
                    tvName.text = p.fullName ?: "User"
                    tvPhone.text = p.phone ?: "Not set"
                    tvLocation.text = p.location ?: "Not set"
                    tvOccupation.text = p.occupation ?: "Not set"
                    tvBio.text = p.bio ?: "No bio added yet."

                    if (!p.avatarUrl.isNullOrEmpty()) {
                        ivProfile.load(p.avatarUrl) {
                            crossfade(true)
                            placeholder(R.drawable.logo)
                            error(R.drawable.logo)
                        }
                    } else {
                        ivProfile.setImageResource(R.drawable.logo)
                    }
                }
            } catch (e: Exception) {
                // Fallback to metadata if DB fails
                val metadata = user.userMetadata
                tvName.text = metadata?.get("full_name")?.toString()?.replace("\"", "") ?: "User"
            }
        }
    }
}

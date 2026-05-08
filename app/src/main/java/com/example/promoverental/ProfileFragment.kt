package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

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
            startActivity(Intent(context, OwnerDashboardActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}

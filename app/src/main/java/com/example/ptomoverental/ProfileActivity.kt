package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set profile data for the logged-in user "kabir"
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvAddress = findViewById<TextView>(R.id.tvAddress)

        tvName.text = "Kabir"
        tvEmail.text = "kabir@promove.com"
        tvRole.text = "House Finder"
        tvPhone.text = "+1 234 567 890"
        tvAddress.text = "123 Main Street, Dhaka, Bangladesh"

        findViewById<android.view.View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardSwitchRole).setOnClickListener {
            startActivity(Intent(this, OwnerDashboardActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptomoverental.adapter.FeaturedHouseAdapter
import com.example.ptomoverental.adapter.HouseAdapter
import com.example.ptomoverental.model.House
import com.google.android.material.bottomnavigation.BottomNavigationView

class FinderDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finder_dashboard)

        findViewById<TextView>(R.id.tvWelcome).text = "Hello, Kabir!"

        val houses = listOf(
            House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment."),
            House("2", "Cozy Studio", "Banani, Dhaka", "$300/mo", 1, 1, "500 sqft", "Perfect for students or singles."),
            House("3", "Luxury Villa", "Uttara, Dhaka", "$1200/mo", 5, 4, "3500 sqft", "Spacious villa with a garden.")
        )

        val rvRecommended = findViewById<RecyclerView>(R.id.rvRecommendedHouses)
        rvRecommended.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRecommended.adapter = FeaturedHouseAdapter(houses) { house ->
            val intent = Intent(this, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }

        val rvRecentlyViewed = findViewById<RecyclerView>(R.id.rvRecentlyViewed)
        rvRecentlyViewed.layoutManager = LinearLayoutManager(this)
        rvRecentlyViewed.adapter = HouseAdapter(houses) { house ->
            val intent = Intent(this, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_finder_dashboard -> {
                    // Already on home
                    true
                }
                R.id.nav_finder_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_finder_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_finder_bookings -> {
                    startActivity(Intent(this, MyBookingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Add logic for Quick Action cards
        findViewById<android.view.View>(R.id.cardSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardMovingService).setOnClickListener {
            startActivity(Intent(this, MovingServiceActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardBookings).setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }
    }
}
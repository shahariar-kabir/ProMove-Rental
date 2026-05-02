package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptomoverental.adapter.HouseAdapter
import com.example.ptomoverental.model.House
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OwnerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, Kabir!"
        findViewById<TextView>(R.id.tvTotalHouses).text = "2"
        findViewById<TextView>(R.id.tvAvailableHouses).text = "1"
        findViewById<TextView>(R.id.tvRentedHouses).text = "1"
        findViewById<TextView>(R.id.tvPendingRequests).text = "3"

        val rvMyHouses = findViewById<RecyclerView>(R.id.rvMyHouses)
        rvMyHouses.layoutManager = LinearLayoutManager(this)

        val myHouses = listOf(
            House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment."),
            House("4", "Office Space", "Dhanmondi, Dhaka", "$800/mo", 0, 1, "1500 sqft", "Ideal for startups.")
        )

        rvMyHouses.adapter = HouseAdapter(myHouses) { house ->
            val intent = Intent(this, HouseDetailsActivity::class.java)
            intent.putExtra("house", house)
            startActivity(intent)
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddHouse)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddHouseActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
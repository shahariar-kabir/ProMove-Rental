package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var rvMyHouses: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var progressBar: ProgressBar
    private lateinit var houseAdapter: HouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        // Initialize Views
        rvMyHouses = findViewById(R.id.rvMyHouses)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)
        progressBar = findViewById(R.id.progressBar)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddHouse)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val btnNotification = findViewById<View>(R.id.btnNotification)
        val tvViewAll = findViewById<TextView>(R.id.tvViewAll)

        // Setup Toolbar
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, Kabir!"
        
        // Setup Stats
        findViewById<TextView>(R.id.tvTotalHouses).text = "2"
        findViewById<TextView>(R.id.tvAvailableHouses).text = "1"
        findViewById<TextView>(R.id.tvRentedHouses).text = "1"
        findViewById<TextView>(R.id.tvPendingRequests).text = "3"

        // Setup RecyclerView
        rvMyHouses.layoutManager = LinearLayoutManager(this)
        loadMyHouses()

        // Listeners
        swipeRefresh.setOnRefreshListener {
            loadMyHouses()
        }

        btnNotification.setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        tvViewAll.setOnClickListener {
            rvMyHouses.smoothScrollToPosition(houseAdapter.itemCount - 1)
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddHouseActivity::class.java))
        }

        findViewById<View>(R.id.btnAddFirstHouse).setOnClickListener {
            startActivity(Intent(this, AddHouseActivity::class.java))
        }

        // Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_my_houses -> {
                    rvMyHouses.smoothScrollToPosition(0)
                    true
                }
                R.id.nav_add_house -> {
                    startActivity(Intent(this, AddHouseActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun loadMyHouses() {
        swipeRefresh.isRefreshing = true
        
        // Simulated data loading
        val myHouses = listOf(
            House("1", "Modern Apartment", "Gulshan, Dhaka", "$500/mo", 3, 2, "1200 sqft", "A beautiful modern apartment."),
            House("4", "Office Space", "Dhanmondi, Dhaka", "$800/mo", 0, 1, "1500 sqft", "Ideal for startups.")
        )

        if (myHouses.isEmpty()) {
            rvMyHouses.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvMyHouses.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            houseAdapter = HouseAdapter(myHouses) { house ->
                val intent = Intent(this, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
            rvMyHouses.adapter = houseAdapter
        }
        
        swipeRefresh.isRefreshing = false
        progressBar.visibility = View.GONE
    }
}

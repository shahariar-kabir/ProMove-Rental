package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.promoverental.adapter.HouseAdapter
import com.example.promoverental.model.House
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var rvMyHouses: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var progressBar: ProgressBar
    private lateinit var houseAdapter: HouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        rvMyHouses = findViewById(R.id.rvMyHouses)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)
        progressBar = findViewById(R.id.progressBar)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddHouse)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val user = SupabaseManager.client.auth.currentUserOrNull()
        val userName = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "Owner"
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $userName!"

        rvMyHouses.layoutManager = LinearLayoutManager(this)
        
        // Initialize Adapter with Owner privileges
        houseAdapter = HouseAdapter(
            houses = emptyList(),
            isOwner = true,
            onEditClick = { house ->
                val intent = Intent(this, AddHouseActivity::class.java)
                intent.putExtra("house", house) // Pass the house to edit
                startActivity(intent)
            },
            onDeleteClick = { house ->
                showDeleteConfirmation(house)
            },
            onItemClick = { house ->
                val intent = Intent(this, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
        )
        rvMyHouses.adapter = houseAdapter

        loadMyHouses()

        swipeRefresh.setOnRefreshListener { loadMyHouses() }
        fabAdd.setOnClickListener { startActivity(Intent(this, AddHouseActivity::class.java)) }
        findViewById<View>(R.id.btnAddFirstHouse).setOnClickListener { startActivity(Intent(this, AddHouseActivity::class.java)) }

        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_my_houses -> { rvMyHouses.smoothScrollToPosition(0); true }
                R.id.nav_add_house -> { startActivity(Intent(this, AddHouseActivity::class.java)); false }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); false }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMyHouses()
    }

    private fun loadMyHouses() {
        swipeRefresh.isRefreshing = true
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

        lifecycleScope.launch {
            try {
                val houses = SupabaseManager.client.postgrest["houses"]
                    .select {
                        filter {
                            eq("owner_id", userId)
                        }
                    }.decodeList<House>()
                updateUI(houses)
            } catch (e: Exception) {
                Toast.makeText(this@OwnerDashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmation(house: House) {
        AlertDialog.Builder(this)
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this listing?")
            .setPositiveButton("Delete") { _, _ ->
                deleteHouse(house)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHouse(house: House) {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest["houses"].delete {
                    filter {
                        eq("id", house.id ?: "")
                    }
                }
                Toast.makeText(this@OwnerDashboardActivity, "Property deleted", Toast.LENGTH_SHORT).show()
                loadMyHouses()
            } catch (e: Exception) {
                Toast.makeText(this@OwnerDashboardActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(houses: List<House>) {
        if (houses.isEmpty()) {
            rvMyHouses.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvMyHouses.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            houseAdapter.updateData(houses)
        }
        findViewById<TextView>(R.id.tvTotalHouses).text = houses.size.toString()
        findViewById<TextView>(R.id.tvAvailableHouses).text = houses.size.toString()
    }
}

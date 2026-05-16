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
import com.example.promoverental.model.Booking
import com.example.promoverental.model.House
import com.example.promoverental.model.Message
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fabAddHouse)
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
        loadPendingCount()

        swipeRefresh.setOnRefreshListener { 
            loadMyHouses()
            loadPendingCount()
        }
        
        findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<View>(R.id.btnMessagesContainer).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

        findViewById<View>(R.id.cardPending).setOnClickListener {
            startActivity(Intent(this, BookingRequestsActivity::class.java))
        }

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
        loadPendingCount()
        loadUnreadMessageCount()
    }

    private fun loadUnreadMessageCount() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        val tvBadge = findViewById<TextView>(R.id.tvMessageBadge)

        lifecycleScope.launch {
            try {
                val count = SupabaseManager.client.postgrest["messages"]
                    .select {
                        filter {
                            eq("receiver_id", userId)
                            eq("is_read", false)
                        }
                    }.decodeList<Message>().size

                if (count > 0) {
                    tvBadge.visibility = View.VISIBLE
                    tvBadge.text = if (count > 99) "99+" else count.toString()
                } else {
                    tvBadge.visibility = View.GONE
                }
            } catch (e: Exception) { }
        }
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
        
        val availableCount = houses.count { it.status == "available" }
        val rentedCount = houses.count { it.status == "rented" }
        
        findViewById<TextView>(R.id.tvTotalHouses).text = houses.size.toString()
        findViewById<TextView>(R.id.tvAvailableHouses).text = availableCount.toString()
        findViewById<TextView>(R.id.tvRentedHouses).text = rentedCount.toString()
    }

    private fun loadPendingCount() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        lifecycleScope.launch {
            try {
                // Fetch bookings for my houses
                val response = SupabaseManager.client.postgrest["bookings"]
                    .select(Columns.raw("*, house:houses(*)")) {
                        filter {
                            eq("house.owner_id", userId)
                            eq("status", "pending")
                        }
                    }.decodeList<Booking>()
                
                findViewById<TextView>(R.id.tvPendingRequests).text = response.size.toString()
            } catch (e: Exception) { }
        }
    }
}

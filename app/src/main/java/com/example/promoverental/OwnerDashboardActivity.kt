package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fabAddHouse)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val user = SupabaseManager.client.auth.currentUserOrNull()
        val userName = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "Owner"
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $userName!"

        loadSummary()
        loadPendingCount()
        loadUnreadMessageCount()

        swipeRefresh.setOnRefreshListener { 
            loadSummary()
            loadPendingCount()
            loadUnreadMessageCount()
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

        findViewById<View>(R.id.btnViewMyHouses).setOnClickListener {
            startActivity(Intent(this, MyHousesActivity::class.java))
        }

        fabAdd.setOnClickListener { startActivity(Intent(this, AddHouseActivity::class.java)) }

        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_my_houses -> { startActivity(Intent(this, MyHousesActivity::class.java)); false }
                R.id.nav_add_house -> { startActivity(Intent(this, AddHouseActivity::class.java)); false }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); false }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSummary()
        loadPendingCount()
        loadUnreadMessageCount()
    }

    private fun loadSummary() {
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
            } finally {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(houses: List<House>) {
        val availableCount = houses.count { it.status == "available" || it.status.isEmpty() }
        val rentedCount = houses.count { it.status == "rented" }
        
        findViewById<TextView>(R.id.tvTotalHouses).text = houses.size.toString()
        findViewById<TextView>(R.id.tvAvailableHouses).text = availableCount.toString()
        findViewById<TextView>(R.id.tvRentedHouses).text = rentedCount.toString()
    }

    private fun loadPendingCount() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        lifecycleScope.launch {
            try {
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

    private fun loadUnreadMessageCount() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
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
}

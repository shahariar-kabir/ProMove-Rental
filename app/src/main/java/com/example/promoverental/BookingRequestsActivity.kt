package com.example.promoverental

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.promoverental.adapter.BookingRequestAdapter
import com.example.promoverental.model.Booking
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class BookingRequestsActivity : AppCompatActivity() {

    private lateinit var adapter: BookingRequestAdapter
    private lateinit var rvRequests: RecyclerView
    private lateinit var emptyState: View
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_requests)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        rvRequests = findViewById(R.id.rvRequests)
        emptyState = findViewById(R.id.emptyState)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        rvRequests.layoutManager = LinearLayoutManager(this)
        adapter = BookingRequestAdapter(
            requests = emptyList(),
            onAccept = { booking -> handleBooking(booking, "confirmed") },
            onReject = { booking -> handleBooking(booking, "rejected") }
        )
        rvRequests.adapter = adapter

        fetchRequests()
        
        swipeRefresh.setOnRefreshListener { fetchRequests() }
    }

    private fun fetchRequests() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                // Fetch bookings for owner's houses. Use !inner to filter by joined table.
                val response = SupabaseManager.client.postgrest["bookings"]
                    .select(Columns.raw("*, house:houses!inner(*)")) {
                        filter {
                            eq("house.owner_id", userId)
                            eq("status", "pending")
                        }
                    }.decodeList<Booking>()

                if (response.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvRequests.visibility = View.GONE
                    adapter.updateData(emptyList())
                } else {
                    emptyState.visibility = View.GONE
                    rvRequests.visibility = View.VISIBLE
                    adapter.updateData(response)
                }
            } catch (e: Exception) {
                Toast.makeText(this@BookingRequestsActivity, "Error fetching: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun handleBooking(booking: Booking, newStatus: String) {
        if (booking.id == null) {
            Toast.makeText(this, "Error: Invalid booking ID", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // 1. Update Booking Status
                SupabaseManager.client.postgrest["bookings"].update(buildJsonObject {
                    put("status", newStatus)
                }) {
                    filter { eq("id", booking.id) }
                }

                // 2. If accepted, mark house as rented
                if (newStatus == "confirmed") {
                    SupabaseManager.client.postgrest["houses"].update(buildJsonObject {
                        put("status", "rented")
                    }) {
                        filter { eq("id", booking.houseId) }
                    }
                    
                    // Optional: Reject all other pending requests for the same house
                    // SupabaseManager.client.postgrest["bookings"].update(buildJsonObject {
                    //     put("status", "rejected")
                    // }) {
                    //     filter { 
                    //         eq("house_id", booking.houseId)
                    //         eq("status", "pending")
                    //         neq("id", booking.id)
                    //     }
                    // }
                }

                // 3. Create Notification for the user
                try {
                    val notificationData = buildJsonObject {
                        put("user_id", booking.userId)
                        put("title", if (newStatus == "confirmed") "Booking Confirmed!" else "Booking Rejected")
                        put("message", "Your request for ${booking.house?.title} has been $newStatus.")
                    }
                    SupabaseManager.client.postgrest["notifications"].insert(notificationData)
                } catch (e: Exception) { }

                Toast.makeText(this@BookingRequestsActivity, "Request $newStatus", Toast.LENGTH_SHORT).show()
                fetchRequests() // Refresh list
            } catch (e: Exception) {
                Toast.makeText(this@BookingRequestsActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}

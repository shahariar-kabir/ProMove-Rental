package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.BookingAdapter
import com.example.promoverental.model.Booking
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class MyBookingsActivity : AppCompatActivity() {
    
    private lateinit var adapter: BookingAdapter
    private lateinit var rvBookings: RecyclerView
    private lateinit var emptyState: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        rvBookings = findViewById(R.id.rvBookings)
        emptyState = findViewById(R.id.emptyState)

        rvBookings.layoutManager = LinearLayoutManager(this)
        adapter = BookingAdapter(emptyList()) { booking ->
            booking.house?.let { house ->
                val intent = Intent(this, HouseDetailsActivity::class.java)
                intent.putExtra("house", house)
                startActivity(intent)
            }
        }
        rvBookings.adapter = adapter

        fetchMyBookings()
    }

    private fun fetchMyBookings() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                // Fetch bookings with house details
                val response = SupabaseManager.client.postgrest["bookings"]
                    .select(Columns.raw("*, house:houses(*)")) {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<Booking>()
                
                if (response.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvBookings.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvBookings.visibility = View.VISIBLE
                    adapter.updateData(response)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyBookingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.promoverental

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.HouseAdapter

class MyBookingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        val rvBookings = findViewById<RecyclerView>(R.id.rvBookings)
        val emptyState = findViewById<View>(R.id.emptyState)

        // For demo, we'll show some dummy bookings
        val bookings = listOf(
            com.example.promoverental.model.House("2", "Cozy Studio", "Banani, Dhaka", "$300/mo", 1, 1, "500 sqft", "Perfect for students.")
        )

        if (bookings.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvBookings.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvBookings.visibility = View.VISIBLE
            rvBookings.layoutManager = LinearLayoutManager(this)
            rvBookings.adapter = HouseAdapter(bookings) { }
        }
    }
}

package com.example.ptomoverental

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptomoverental.adapter.HouseAdapter

class MyBookingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        findViewById<View>(R.id.toolbar).setOnClickListener { finish() }

        val rvBookings = findViewById<RecyclerView>(R.id.rvBookings)
        val tvNoBookings = findViewById<TextView>(R.id.tvNoBookings)

        // For demo, we'll show an empty state or some dummy bookings
        val bookings = emptyList<com.example.ptomoverental.model.House>()

        if (bookings.isEmpty()) {
            tvNoBookings.visibility = View.VISIBLE
            rvBookings.visibility = View.GONE
        } else {
            tvNoBookings.visibility = View.GONE
            rvBookings.visibility = View.VISIBLE
            rvBookings.layoutManager = LinearLayoutManager(this)
            rvBookings.adapter = HouseAdapter(bookings) { }
        }
    }
}
package com.example.promoverental

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.promoverental.model.MovingProvider

class BookMovingServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_moving_service)

        val provider = intent.getSerializableExtra("provider") as? MovingProvider

        provider?.let {
            findViewById<TextView>(R.id.tvProviderName).text = it.name
            findViewById<TextView>(R.id.tvProviderRating).text = "${it.rating} (${it.reviews} reviews)"
            findViewById<TextView>(R.id.tvProviderPrice).text = it.pricePerKm
        }

        findViewById<android.view.View>(R.id.toolbar).setOnClickListener {
            finish()
        }

        findViewById<android.view.View>(R.id.btnConfirmBooking).setOnClickListener {
            Toast.makeText(this, "Booking request sent successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

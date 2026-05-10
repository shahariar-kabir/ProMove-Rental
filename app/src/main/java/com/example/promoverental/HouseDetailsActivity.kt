package com.example.promoverental

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.promoverental.model.House

class HouseDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_details)

        val house = intent.getSerializableExtra("house") as? House

        house?.let {
            findViewById<TextView>(R.id.tvTitle).text = it.title
            findViewById<TextView>(R.id.tvLocation).text = it.location
            findViewById<TextView>(R.id.tvPrice).text = it.price
            findViewById<TextView>(R.id.tvBedrooms).text = it.bedrooms.toString()
            findViewById<TextView>(R.id.tvBathrooms).text = it.bathrooms.toString()
            findViewById<TextView>(R.id.tvArea).text = it.area
            findViewById<TextView>(R.id.tvDescription).text = it.description
            findViewById<TextView>(R.id.tvOwnerName).text = "House Owner"
            findViewById<TextView>(R.id.tvOwnerPhone).text = "+880 1234 567890"

            // Image loading via Coil
            // Note: We are using logo as fallback, but it loads imageUrl from Supabase
            findViewById<ImageView>(R.id.ivHouseImage)?.load(it.imageUrl) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        }

        findViewById<View>(R.id.toolbar).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnBookNow).setOnClickListener {
            Toast.makeText(this, "House booking request sent to owner!", Toast.LENGTH_LONG).show()
        }

        findViewById<View>(R.id.btnFavorite).setOnClickListener {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnCallOwner).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+8801234567890")
            startActivity(intent)
        }
    }
}

package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promoverental.adapter.MovingProviderAdapter
import com.example.promoverental.model.MovingProvider

class MovingServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moving_service)

        findViewById<android.view.View>(R.id.toolbar).setOnClickListener {
            finish()
        }

        val rvProviders = findViewById<RecyclerView>(R.id.rvProviders)
        rvProviders.layoutManager = LinearLayoutManager(this)

        val providers = listOf(
            MovingProvider("1", "Fast Movers", 4.8f, 120, "$10/km", "Best in class moving service."),
            MovingProvider("2", "Safe & Sound", 4.5f, 85, "$8/km", "We handle your items with care."),
            MovingProvider("3", "Eco Move", 4.2f, 50, "$12/km", "Environment friendly moving.")
        )

        rvProviders.adapter = MovingProviderAdapter(providers) { provider ->
            val intent = Intent(this, BookMovingServiceActivity::class.java)
            intent.putExtra("provider", provider)
            startActivity(intent)
        }
    }
}

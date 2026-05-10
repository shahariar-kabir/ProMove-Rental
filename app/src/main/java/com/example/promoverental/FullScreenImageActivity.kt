package com.example.promoverental

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.load

class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageUrl = intent.getStringExtra("image_url")
        val imageView = findViewById<ImageView>(R.id.ivFullScreen)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        imageView.load(imageUrl) {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}

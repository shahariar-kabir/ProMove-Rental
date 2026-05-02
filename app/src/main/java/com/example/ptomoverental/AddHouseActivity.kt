package com.example.ptomoverental

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class AddHouseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_house)

        findViewById<View>(R.id.btnPublish).setOnClickListener {
            finish()
        }
    }
}
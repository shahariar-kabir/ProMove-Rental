package com.example.promoverental

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddHouseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_house)

        findViewById<View>(R.id.toolbar).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnPublish).setOnClickListener {
            Toast.makeText(this, "House published successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

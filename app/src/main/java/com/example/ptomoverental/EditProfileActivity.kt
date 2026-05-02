package com.example.ptomoverental

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViewById<EditText>(R.id.etFullName).setText("Kabir")
        findViewById<EditText>(R.id.etEmail).setText("kabir@promove.com")
        findViewById<EditText>(R.id.etPhone).setText("+1 234 567 890")
        findViewById<EditText>(R.id.etAddress).setText("123 Main Street, Dhaka, Bangladesh")

        findViewById<android.view.View>(R.id.toolbar).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            // In a real app, save data. For now, just close.
            finish()
        }
    }
}
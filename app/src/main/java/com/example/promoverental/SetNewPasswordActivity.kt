package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class SetNewPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_new_password)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnUpdatePassword).setOnClickListener {
            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
            // Password updated, go back to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}

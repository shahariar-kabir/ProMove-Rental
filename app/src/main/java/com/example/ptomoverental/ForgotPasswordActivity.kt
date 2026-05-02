package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val btnReset = findViewById<MaterialButton>(R.id.btnResetPassword)
        btnReset.setOnClickListener {
            // In a real app, send reset email. For now, navigate to verification.
            val intent = Intent(this, EmailVerificationActivity::class.java)
            intent.putExtra("isFromForgotPassword", true)
            startActivity(intent)
        }
    }
}
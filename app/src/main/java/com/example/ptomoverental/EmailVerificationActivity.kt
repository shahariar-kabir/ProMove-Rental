package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class EmailVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        val btnVerify = findViewById<MaterialButton>(R.id.btnVerifyCode)
        btnVerify.setOnClickListener {
            val isFromForgotPassword = intent.getBooleanExtra("isFromForgotPassword", false)
            if (isFromForgotPassword) {
                startActivity(Intent(this, SetNewPasswordActivity::class.java))
            } else {
                startActivity(Intent(this, FinderDashboardActivity::class.java))
                finishAffinity()
            }
        }
    }
}
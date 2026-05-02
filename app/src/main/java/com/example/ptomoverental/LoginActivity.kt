package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tabSignUp = findViewById<android.view.View>(R.id.tabSignUp)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnContinue.setOnClickListener {
            val username = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username == "kabir" && password == "123456") {
                // Successful login
                startActivity(Intent(this, FinderDashboardActivity::class.java))
                finish() // Optional: close login activity
            } else {
                // Failed login
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        val goToSignUp = {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        tvSignUp.setOnClickListener { goToSignUp() }
        tabSignUp.setOnClickListener { goToSignUp() }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
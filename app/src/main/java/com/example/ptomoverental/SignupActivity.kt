package com.example.ptomoverental

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnSignUp = findViewById<MaterialButton>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val tabLogin = findViewById<android.view.View>(R.id.tabLogin)

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, EmailVerificationActivity::class.java))
        }

        val goToLogin = {
            finish()
        }

        tvLogin.setOnClickListener { goToLogin() }
        tabLogin.setOnClickListener { goToLogin() }
    }
}
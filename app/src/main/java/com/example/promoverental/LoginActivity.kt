package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import androidx.lifecycle.lifecycleScope
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

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
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    
                    val user = SupabaseManager.client.auth.currentUserOrNull()
                    val role = user?.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "finder"
                    
                    if (role == "owner") {
                        startActivity(Intent(this@LoginActivity, OwnerDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                    finish()
                    
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

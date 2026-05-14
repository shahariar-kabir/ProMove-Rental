package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.promoverental.utils.SupabaseManager
import io.github.jan.supabase.auth.auth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    private fun checkUserSession() {
        val user = SupabaseManager.client.auth.currentUserOrNull()
        
        if (user != null) {
            // User is already logged in, check role and redirect
            val role = user.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "finder"
            
            if (role == "owner") {
                startActivity(Intent(this, OwnerDashboardActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            // No user session, go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}

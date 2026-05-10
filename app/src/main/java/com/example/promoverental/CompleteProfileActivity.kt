package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CompleteProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        val name = intent.getStringExtra("name") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        val etAge = findViewById<EditText>(R.id.etAge)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etProfession = findViewById<EditText>(R.id.etProfession)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val btnFinish = findViewById<MaterialButton>(R.id.btnFinish)

        btnFinish.setOnClickListener {
            val age = etAge.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val profession = etProfession.text.toString().trim()
            val role = if (rgRole.checkedRadioButtonId == R.id.rbOwner) "owner" else "finder"

            if (age.isEmpty() || location.isEmpty() || profession.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Supabase Sign Up with all details
            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                        data = buildJsonObject {
                            put("full_name", name)
                            put("age", age)
                            put("location", location)
                            put("profession", profession)
                            put("role", role)
                        }
                    }
                    
                    Toast.makeText(this@CompleteProfileActivity, "Registration success! Check email.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@CompleteProfileActivity, EmailVerificationActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                    
                } catch (e: Exception) {
                    Toast.makeText(this@CompleteProfileActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

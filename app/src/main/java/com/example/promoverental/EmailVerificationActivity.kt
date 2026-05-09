package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.promoverental.utils.SupabaseManager
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var otp5: EditText
    private lateinit var otp6: EditText
    private lateinit var btnVerify: MaterialButton
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        userEmail = intent.getStringExtra("email")

        // Initialize Views
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvResendEmail = findViewById<TextView>(R.id.tvResendEmail)
        btnVerify = findViewById(R.id.btnVerifyCode)
        
        otp1 = findViewById(R.id.otpDigit1)
        otp2 = findViewById(R.id.otpDigit2)
        otp3 = findViewById(R.id.otpDigit3)
        otp4 = findViewById(R.id.otpDigit4)
        otp5 = findViewById(R.id.otpDigit5)
        otp6 = findViewById(R.id.otpDigit6)

        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Resend Email
        tvResendEmail.setOnClickListener {
            resendOtp()
        }

        // OTP Input Logic
        setupOtpInputs()

        // Verify Button
        btnVerify.setOnClickListener {
            verifyCode()
        }
        
        // Initial state check
        checkAllFields()
    }

    private fun resendOtp() {
        userEmail?.let { email ->
            lifecycleScope.launch {
                try {
                    // Supabase sends a new OTP when you try to sign in/up again or via specialized reset calls
                    Toast.makeText(this@EmailVerificationActivity, "Resending code to $email...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@EmailVerificationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyCode() {
        val code = "${otp1.text}${otp2.text}${otp3.text}${otp4.text}${otp5.text}${otp6.text}"
        val email = userEmail ?: return

        lifecycleScope.launch {
            try {
                // In Supabase KT 3.x, use verifyEmailOtp
                SupabaseManager.client.auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = email,
                    token = code
                )
                
                val isFromForgotPassword = intent.getBooleanExtra("isFromForgotPassword", false)
                if (isFromForgotPassword) {
                    startActivity(Intent(this@EmailVerificationActivity, SetNewPasswordActivity::class.java))
                } else {
                    startActivity(Intent(this@EmailVerificationActivity, MainActivity::class.java))
                    finishAffinity()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EmailVerificationActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupOtpInputs() {
        val otpFields = listOf(otp1, otp2, otp3, otp4, otp5, otp6)
        
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < otpFields.size - 1) {
                            otpFields[i + 1].requestFocus()
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    checkAllFields()
                }
            })

            // Handle backspace to move focus back
            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && 
                    event.action == KeyEvent.ACTION_DOWN &&
                    otpFields[i].text.isEmpty() && i > 0) {
                    otpFields[i - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun checkAllFields() {
        val isFilled = otp1.text.isNotEmpty() && 
                      otp2.text.isNotEmpty() && 
                      otp3.text.isNotEmpty() && 
                      otp4.text.isNotEmpty() && 
                      otp5.text.isNotEmpty() &&
                      otp6.text.isNotEmpty()
        
        btnVerify.isEnabled = isFilled
        btnVerify.alpha = if (isFilled) 1.0f else 0.5f
    }
}

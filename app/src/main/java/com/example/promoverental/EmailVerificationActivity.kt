package com.example.promoverental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var otp5: EditText
    private lateinit var btnVerify: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        // Initialize Views
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvResendEmail = findViewById<TextView>(R.id.tvResendEmail)
        btnVerify = findViewById(R.id.btnVerifyCode)
        
        otp1 = findViewById(R.id.otpDigit1)
        otp2 = findViewById(R.id.otpDigit2)
        otp3 = findViewById(R.id.otpDigit3)
        otp4 = findViewById(R.id.otpDigit4)
        otp5 = findViewById(R.id.otpDigit5)

        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Resend Email
        tvResendEmail.setOnClickListener {
            // Handle resend logic here (e.g., call API to resend OTP)
        }

        // OTP Input Logic
        setupOtpInputs()

        // Verify Button
        btnVerify.setOnClickListener {
            val isFromForgotPassword = intent.getBooleanExtra("isFromForgotPassword", false)
            if (isFromForgotPassword) {
                startActivity(Intent(this, SetNewPasswordActivity::class.java))
            } else {
                // Navigating to MainActivity which acts as the Finder Dashboard
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }
        
        // Initial state check
        checkAllFields()
    }

    private fun setupOtpInputs() {
        val otpFields = listOf(otp1, otp2, otp3, otp4, otp5)
        
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
                      otp5.text.isNotEmpty()
        
        btnVerify.isEnabled = isFilled
        btnVerify.alpha = if (isFilled) 1.0f else 0.5f
    }
}

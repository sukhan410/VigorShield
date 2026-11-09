package com.vigorshield.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("VigorPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_onboarded", false)) {
            renderDashboard()
            return
        }

        renderOnboarding()
    }

    private fun renderOnboarding() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
        }

        val title = TextView(this).apply {
            text = "🛡️ Vigor Shield Setup"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        layout.addView(title)

        val etName = EditText(this).apply { hint = "Full Name" }
        val etAge = EditText(this).apply { 
            hint = "Age" 
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etUserPhone = EditText(this).apply { 
            hint = "Your Mobile (10 digits)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val etGuardianPhone = EditText(this).apply { 
            hint = "Guardian Mobile (Cannot be your phone)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val etPin = EditText(this).apply { 
            hint = "4-Digit Guardian PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        layout.addView(etName)
        layout.addView(etAge)
        layout.addView(etUserPhone)
        layout.addView(etGuardianPhone)
        layout.addView(etPin)

        val btnActivate = Button(this).apply {
            text = "Turn On Protection"
            setOnClickListener {
                val name = etName.text.toString().trim()
                val age = etAge.text.toString().trim()
                val userPhone = etUserPhone.text.toString().trim().replace("+91", "")
                val guardianPhone = etGuardianPhone.text.toString().trim().replace("+91", "")
                val pin = etPin.text.toString().trim()

                if (name.isEmpty() || age.isEmpty()) {
                    Toast.makeText(this@OnboardingActivity, "Please complete name and age.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (userPhone.length != 10 || guardianPhone.length != 10) {
                    Toast.makeText(this@OnboardingActivity, "Enter valid 10-digit mobile numbers.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (userPhone == guardianPhone) {
                    Toast.makeText(this@OnboardingActivity, "Guardian phone CANNOT be identical to user phone!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (pin.length != 4) {
                    Toast.makeText(this@OnboardingActivity, "Guardian PIN must be exactly 4 digits.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val prefs = getSharedPreferences("VigorPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_name", name)
                    .putInt("user_age", age.toInt())
                    .putString("user_phone", userPhone)
                    .putString("guardian_phone", guardianPhone)
                    .putString("guardian_pin", pin)
                    .putBoolean("is_onboarded", true)
                    .putInt("reels_skipped", 0)
                    .apply()

                Toast.makeText(this@OnboardingActivity, "Profile Saved! Now enable Accessibility.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                finish()
            }
        }
        layout.addView(btnActivate)
        setContentView(layout)
    }

    private fun renderDashboard() {
        val prefs = getSharedPreferences("VigorPrefs", Context.MODE_PRIVATE)
        val skippedCount = prefs.getInt("reels_skipped", 0)
        val userName = prefs.getString("user_name", "User")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
        }

        val tvWelcome = TextView(this).apply {
            text = "Welcome, $userName\nStatus: SHIELD ACTIVE"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        layout.addView(tvWelcome)

        val tvStats = TextView(this).apply {
            text = "\nReal-Time Skipped Reels: $skippedCount\nClean Streak: Active"
            textSize = 18f
        }
        layout.addView(tvStats)

        val btnOpenAccessibility = Button(this).apply {
            text = "Verify Accessibility Permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        layout.addView(btnOpenAccessibility)

        setContentView(layout)
    }
}

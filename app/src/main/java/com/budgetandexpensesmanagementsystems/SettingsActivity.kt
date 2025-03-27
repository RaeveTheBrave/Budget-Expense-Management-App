package com.budgetandexpensesmanagementsystems

import android.content.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var notificationSwitch: Switch
    private lateinit var currencySpinner: Spinner
    private lateinit var resetDataButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Initialize views
        notificationSwitch = findViewById(R.id.notificationSwitch)
        currencySpinner = findViewById(R.id.currencySpinner)
        resetDataButton = findViewById(R.id.resetDataButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_settings

        // Handle bottom navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_values -> {
                    startActivity(Intent(this, ValuesActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                R.id.nav_settings -> true // Stay on settings page
                else -> false
            }
        }

        // Load saved notification setting
        loadNotificationSetting()

        // Toggle notification setting
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSetting(isChecked)
        }

        // Change currency function
        setupCurrencySelection()

        // Reset data function
        resetDataButton.setOnClickListener {
            resetFirestoreData()
        }

        // Reset password function
        changePasswordButton.setOnClickListener {
            resetPassword()
        }
    }

    // Saves the notification setting to SharedPreferences
    private fun saveNotificationSetting(isEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("notifications", isEnabled).apply()
        val message = if (isEnabled) "Notifications Enabled" else "Notifications Disabled"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Loads the notification setting from SharedPreferences
    private fun loadNotificationSetting() {
        val sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("notifications", true)
        notificationSwitch.isChecked = isEnabled
    }

    private fun resetFirestoreData() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to reset all data? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // Delete Firestore budget data
                db.collection("budget").document("values")
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Budget data reset successfully.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to reset budget data.", Toast.LENGTH_SHORT).show()
                    }

                // Delete user authentication account
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null) // Dismiss if canceled
            .show()
    }

    // Handles currency selection changes
    private fun setupCurrencySelection() {
        val currencyOptions = arrayOf("USD($) ", "EUR(€) ", "GBP(£) ", "PHP(₱) ")
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencyOptions)
        currencySpinner.adapter = adapter

        // Load saved currency
        val savedCurrency = sharedPreferences.getString("currency", "USD($) ")
        val position = currencyOptions.indexOf(savedCurrency)
        if (position >= 0) currencySpinner.setSelection(position)

        var isFirstSelection = true  // Flag to track initial selection

        // Change currency when user selects a new one
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencyOptions[position]

                // Only show toast if this is NOT the initial selection
                if (!isFirstSelection) {
                    saveCurrencyPreference(selectedCurrency)
                    Toast.makeText(this@SettingsActivity, "Currency updated to $selectedCurrency", Toast.LENGTH_SHORT).show()
                }
                isFirstSelection = false  // Update flag after first selection
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Save selected currency in SharedPreferences
    private fun saveCurrencyPreference(currency: String) {
        sharedPreferences.edit().putString("currency", currency).apply()
        Toast.makeText(this, "Currency updated to $currency", Toast.LENGTH_SHORT).show()
    }

    // Sends a password reset email to the logged-in user
    private fun resetPassword() {
        val user = auth.currentUser
        if (user != null) {
            auth.sendPasswordResetEmail(user.email!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show()
        }
    }
}
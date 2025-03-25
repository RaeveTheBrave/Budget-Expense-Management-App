package com.budgetandexpensesmanagementsystems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_settings

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
                R.id.nav_statistics -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                R.id.nav_settings -> true // Stay on the settings page
                else -> false
            }
        }

        val notificationSwitch: Switch = findViewById(R.id.notificationSwitch)
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        val resetDataButton: Button = findViewById(R.id.resetDataButton)
        resetDataButton.setOnClickListener {
            Toast.makeText(this, "Data Reset Successful", Toast.LENGTH_SHORT).show()
            // Add logic for resetting data
        }
    }
}
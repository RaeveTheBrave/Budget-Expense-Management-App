package com.budgetandexpensesmanagementsystems

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var tvWalletAmount: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpenses: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvWalletAmount = findViewById(R.id.tvWalletAmount)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpenses = findViewById(R.id.tvExpenses)

        loadValuesFromFirestore()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_values -> {
                    startActivity(Intent(this, ValuesActivity::class.java))
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadValuesFromFirestore() {
        db.collection("budget").document("userValues")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    tvWalletAmount.text = "Wallet Amount: $" + (document.getDouble("walletAmount") ?: 0.0)
                    tvIncome.text = "Income: $" + (document.getDouble("income") ?: 0.0)
                    tvExpenses.text = "Expenses: $" + (document.getDouble("expenses") ?: 0.0)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading values", Toast.LENGTH_SHORT).show()
            }
    }
}
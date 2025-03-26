package com.budgetandexpensesmanagementsystems

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
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

        // For initializing views
        tvWalletAmount = findViewById(R.id.tvWalletAmount)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpenses = findViewById(R.id.tvExpenses)

        val cardDate = findViewById<androidx.cardview.widget.CardView>(R.id.cardDate)
        val scrollView = findViewById<ScrollView>(R.id.scView)
        val cardDisp = findViewById<androidx.cardview.widget.CardView>(R.id.cardDisposableIncome)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Loading data from Firestore
        loadValuesFromFirestore()

        // Card On Click Listener
        cardDate.setOnClickListener{
            startActivity(Intent(this, StatisticsActivity::class.java ))
        }
        // Scroll View On Click Listener
        scrollView.setOnClickListener{
            startActivity(Intent(this, StatisticsActivity::class.java ))
        }
        // Card click listener
        cardDisp.setOnClickListener{
            startActivity(Intent(this, ValuesActivity::class.java))
        }

        // Bottom navigation
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
        db.collection("budget").document("values")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val wallet = (document.get("wallet") as? Number)?.toDouble() ?: 0.0
                    val income = (document.get("income") as? Number)?.toDouble() ?: 0.0
                    val expenses = (document.get("expenses") as? Number)?.toDouble() ?: 0.0

                    tvWalletAmount.text = "Wallet Amount: $${"%.2f".format(wallet)}"
                    tvIncome.text = "Income: $${"%.2f".format(income)}"
                    tvExpenses.text = "Expenses: $${"%.2f".format(expenses)}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading values", Toast.LENGTH_SHORT).show()
            }
    }
}
package com.budgetandexpensesmanagementsystems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class ValuesActivity : AppCompatActivity() {
    private lateinit var etWallet: EditText
    private lateinit var etIncome: EditText
    private lateinit var etExpenses: EditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_values)

        etWallet = findViewById(R.id.inputWallet)
        etIncome = findViewById(R.id.inputIncome)
        etExpenses = findViewById(R.id.inputExpenses)
        btnSave = findViewById(R.id.btnSave)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnSave.setOnClickListener {
            saveValuesToFirestore()
        }

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

    private fun saveValuesToFirestore() {
        val wallet = etWallet.text.toString().toDoubleOrNull() ?: 0.0
        val income = etIncome.text.toString().toDoubleOrNull() ?: 0.0
        val expenses = etExpenses.text.toString().toDoubleOrNull() ?: 0.0

        val values = hashMapOf(
            "wallet" to wallet,
            "income" to income,
            "expenses" to expenses
        )

        db.collection("budget").document("values")
            .set(values)
            .addOnSuccessListener {
                Toast.makeText(this, "Values saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving values", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadValuesFromFirestore() {
        db.collection("budget").document("values")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etWallet.setText(
                        (document.get("wallet") as? Number)?.toDouble()?.toString() ?: "0"
                    )
                    etIncome.setText(
                        (document.get("income") as? Number)?.toDouble()?.toString() ?: "0"
                    )
                    etExpenses.setText(
                        (document.get("income") as? Number)?.toDouble()?.toString() ?: "0"
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading values", Toast.LENGTH_SHORT).show()
            }
    }
}
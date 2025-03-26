package com.budgetandexpensesmanagementsystems

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var tvWalletAmount: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvDisposableIncome: TextView
    private lateinit var progressBarDisposableIncome: ProgressBar
    private lateinit var recentIncomeExpensesContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvDisposableIncome = findViewById(R.id.tvDisposableIncome)
        progressBarDisposableIncome = findViewById(R.id.progressBarDisposableIncome)
        tvWalletAmount = findViewById(R.id.tvWalletAmount)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpenses = findViewById(R.id.tvExpenses)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        recentIncomeExpensesContainer = findViewById(R.id.recentIncomeExpensesContainer)

        val cardDate = findViewById<androidx.cardview.widget.CardView>(R.id.cardDate)
        val scrollView = findViewById<ScrollView>(R.id.scView)
        val cardDisp = findViewById<androidx.cardview.widget.CardView>(R.id.cardDisposableIncome)
        val cardWallet = findViewById<androidx.cardview.widget.CardView>(R.id.cardWallet)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Load Firestore data
        loadValuesFromFirestore()
        loadRecentIncomeAndExpenses()
        loadTotalIncomeAndExpenses()

        // Set the current date
        setCurrentDate()

        // Card Click Listeners
        cardDate.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
        scrollView.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
        cardDisp.setOnClickListener {
            startActivity(Intent(this, ValuesActivity::class.java))
        }
        cardWallet.setOnClickListener {
            startActivity(Intent(this, ValuesActivity::class.java))
        }

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Stay on this page
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
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        tvCurrentDate.text = "Current Date: $currentDate"
    }

    private fun loadValuesFromFirestore() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val currency = sharedPreferences.getString("currency", "USD($) ") ?: "USD($) "

        db.collection("budget").document("values")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val wallet = (document.get("wallet") as? Number)?.toDouble() ?: 0.0
                    val disposableIncome = (document.get("disposable_income") as? Number)?.toDouble() ?: 0.0

                    tvDisposableIncome.text = "Disposable Income: $currency${"%.2f".format(disposableIncome)}"
                    tvWalletAmount.text = "Wallet Amount: $currency${"%.2f".format(wallet)}"

                    val maxIncome = 1000.0 // Adjust based on actual max income in your system
                    val progress = ((disposableIncome / maxIncome) * 100).toInt()
                    progressBarDisposableIncome.progress = progress
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading values", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTotalIncomeAndExpenses() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val currency = sharedPreferences.getString("currency", "USD($) ") ?: "USD($) "

        // Fetch total income
        db.collection("budget").document("values").collection("income")
            .get()
            .addOnSuccessListener { incomeDocs ->
                var totalIncome = 0.0
                for (document in incomeDocs) {
                    totalIncome += (document.get("amount") as? Number)?.toDouble() ?: 0.0
                }
                tvIncome.text = "Total Income: $currency${"%.2f".format(totalIncome)}"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading total income", Toast.LENGTH_SHORT).show()
            }

        // Fetch total expenses
        db.collection("budget").document("values").collection("expenses")
            .get()
            .addOnSuccessListener { expenseDocs ->
                var totalExpenses = 0.0
                for (document in expenseDocs) {
                    totalExpenses += (document.get("amount") as? Number)?.toDouble() ?: 0.0
                }
                tvExpenses.text = "Total Expenses: $currency${"%.2f".format(totalExpenses)}"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading total expenses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRecentIncomeAndExpenses() {
        recentIncomeExpensesContainer.removeAllViews() // Clear old views

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val currency = sharedPreferences.getString("currency", "USD($) ") ?: "USD($) "

        // Fetch recent 5 income transactions
        db.collection("budget").document("values").collection("income")
            .orderBy("date")
            .limit(5)
            .get()
            .addOnSuccessListener { incomeDocs ->
                for (document in incomeDocs) {
                    val id = document.id // Document ID for deletion
                    val description = document.getString("note") ?: "Unknown"
                    val amount = (document.get("amount") as? Number)?.toDouble() ?: 0.0
                    val date = document.getString("date") ?: "No Date"

                    val incomeTextView = TextView(this)
                    incomeTextView.text = "Income: $description - $currency$amount ($date)"
                    incomeTextView.textSize = 16f
                    incomeTextView.setPadding(8, 8, 8, 8)

                    // Long-click listener for deletion
                    incomeTextView.setOnLongClickListener {
                        showDeleteConfirmationDialog("income", id)
                        true
                    }
                    recentIncomeExpensesContainer.addView(incomeTextView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading income", Toast.LENGTH_SHORT).show()
            }

        // Fetch recent 5 expense transactions
        db.collection("budget").document("values").collection("expenses")
            .orderBy("date")
            .limit(5)
            .get()
            .addOnSuccessListener { expenseDocs ->
                for (document in expenseDocs) {
                    val id = document.id // Document ID for deletion
                    val description = document.getString("note") ?: "Unknown"
                    val amount = (document.get("amount") as? Number)?.toDouble() ?: 0.0
                    val date = document.getString("date") ?: "No Date"

                    val expenseTextView = TextView(this)
                    expenseTextView.text = "Expense: $description - $currency$amount ($date)"
                    expenseTextView.textSize = 16f
                    expenseTextView.setPadding(8, 8, 8, 8)

                    // Long-click listener for deletion
                    expenseTextView.setOnLongClickListener {
                        showDeleteConfirmationDialog("expenses", id)
                        true
                    }
                    recentIncomeExpensesContainer.addView(expenseTextView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading expenses", Toast.LENGTH_SHORT).show()
            }
    }

    // Show a confirmation dialog before deleting an income or expense entry
    private fun showDeleteConfirmationDialog(collection: String, docId: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Delete Entry")
        builder.setMessage("Are you sure you want to delete this entry? This action cannot be undone.")

        builder.setPositiveButton("Delete") { _, _ ->
            deleteTransaction(collection, docId)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // Deletes an income or expense transaction from Firestore
    private fun deleteTransaction(collection: String, docId: String) {
        db.collection("budget").document("values").collection(collection).document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Entry deleted successfully.", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete entry.", Toast.LENGTH_SHORT).show()
            }
    }
}
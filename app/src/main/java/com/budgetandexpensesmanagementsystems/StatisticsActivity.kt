package com.budgetandexpensesmanagementsystems

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class StatisticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var summaryTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Initialize UI Elements
        pieChart = findViewById(R.id.pieChart)
        summaryTextView = findViewById(R.id.statisticsSummary)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)

        // Set selected item in Bottom Navigation
        bottomNavigation.selectedItemId = R.id.nav_statistics

        // Bottom Navigation Item Selection
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
                R.id.nav_statistics -> true // Stay on this page
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Load and update statistics
        loadStatistics()
    }

    private fun loadStatistics() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val currency = sharedPreferences.getString("currency", "USD($) ") ?: "USD($) "

        var totalIncome = 0.0
        var totalExpenses = 0.0

        // Fetch income data from Firestore
        db.collection("budget").document("values").collection("income")
            .get()
            .addOnSuccessListener { incomeDocs ->
                for (document in incomeDocs) {
                    totalIncome += (document.get("amount") as? Number)?.toDouble() ?: 0.0
                }

                // Fetch expense data after income
                db.collection("budget").document("values").collection("expenses")
                    .get()
                    .addOnSuccessListener { expenseDocs ->
                        for (document in expenseDocs) {
                            totalExpenses += (document.get("amount") as? Number)?.toDouble() ?: 0.0
                        }

                        // Calculate savings
                        val savings = totalIncome - totalExpenses

                        // Update the summary text dynamically
                        summaryTextView.text = "Total Income: $currency${"%.2f".format(totalIncome)}" +
                                "\n\nTotal Expenses: $currency${"%.2f".format(totalExpenses)}" +
                                "\n\nSavings: $currency${"%.2f".format(savings)}"

                        // Update Pie Chart visualization
                        updatePieChart(totalIncome, totalExpenses)
                    }
            }
    }

    private fun updatePieChart(income: Double, expenses: Double) {
        val entries = ArrayList<PieEntry>()
        if (income > 0) entries.add(PieEntry(income.toFloat(), "Income"))
        if (expenses > 0) entries.add(PieEntry(expenses.toFloat(), "Expenses"))

        val dataSet = PieDataSet(entries, "Overview")
        dataSet.colors = listOf(Color.LTGRAY, Color.GRAY)
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.invalidate() // Refresh chart
    }
}
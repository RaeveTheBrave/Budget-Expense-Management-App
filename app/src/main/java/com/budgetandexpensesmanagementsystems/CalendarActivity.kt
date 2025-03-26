package com.budgetandexpensesmanagementsystems

import android.content.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var tvSelectedDate: TextView
    private lateinit var rvIncomeExpenses: RecyclerView
    private lateinit var tvNoIncomeExpenses: TextView
    private lateinit var IncomeExpensesAdapter: IncomeExpensesAdapter

    private val db = FirebaseFirestore.getInstance()
    private val incomeExpensesList = mutableListOf<IncomeExpenses>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize UI components
        calendarView = findViewById(R.id.calendarView)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        rvIncomeExpenses = findViewById(R.id.rvIncomeExpenses)
        tvNoIncomeExpenses = findViewById(R.id.tvNoIncomeExpenses)

        // Set up RecyclerView
        IncomeExpensesAdapter = IncomeExpensesAdapter(this, incomeExpensesList)
        rvIncomeExpenses.layoutManager = LinearLayoutManager(this)
        rvIncomeExpenses.adapter = IncomeExpensesAdapter

        // Get today's date as default
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date())

        // Load income expenses for today's date
        loadIncomeExpensesForDate(todayDate)

        // Handle date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            tvSelectedDate.text = "Selected Date: $selectedDate"
            loadIncomeExpensesForDate(selectedDate)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_calendar

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
                R.id.nav_calendar -> true // Stay on this page
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

    private fun loadIncomeExpensesForDate(date: String) {
        incomeExpensesList.clear()

        db.collection("budget").document("values").collection("income")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { incomeDocs ->
                for (doc in incomeDocs) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    val note = doc.getString("note") ?: ""
                    incomeExpensesList.add(IncomeExpenses("Income", amount, note, date))
                }

                db.collection("budget").document("values").collection("expenses")
                    .whereEqualTo("date", date)
                    .get()
                    .addOnSuccessListener { expenseDocs ->
                        for (doc in expenseDocs) {
                            val amount = doc.getDouble("amount") ?: 0.0
                            val note = doc.getString("note") ?: ""
                            incomeExpensesList.add(IncomeExpenses("Expense", amount, note, date))
                        }

                        // Update RecyclerView
                        if (incomeExpensesList.isEmpty()) {
                            tvNoIncomeExpenses.visibility = TextView.VISIBLE
                            rvIncomeExpenses.visibility = RecyclerView.GONE
                        } else {
                            tvNoIncomeExpenses.visibility = TextView.GONE
                            rvIncomeExpenses.visibility = RecyclerView.VISIBLE
                            IncomeExpensesAdapter.notifyDataSetChanged()
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading dates", Toast.LENGTH_SHORT).show()
            }
    }
}
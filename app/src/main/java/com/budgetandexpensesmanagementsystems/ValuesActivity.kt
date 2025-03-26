package com.budgetandexpensesmanagementsystems

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ValuesActivity : AppCompatActivity() {
    private lateinit var etWallet: EditText
    private lateinit var btnSaveWallet: Button

    private lateinit var etIncome: EditText
    private lateinit var etIncomeNote: EditText
    private lateinit var inputIncomeDate: EditText
    private lateinit var btnSaveIncome: Button

    private lateinit var etExpenses: EditText
    private lateinit var etExpensesNote: EditText
    private lateinit var inputExpensesDate: EditText
    private lateinit var btnSaveExpenses: Button

    private lateinit var etDisposableIncome: EditText
    private lateinit var btnSaveDisposableIncome: Button

    private val db = FirebaseFirestore.getInstance()
    private var selectedIncomeDate: String = ""
    private var selectedExpensesDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_values)

        etWallet = findViewById(R.id.inputWallet)
        btnSaveWallet = findViewById(R.id.btnSaveWallet)

        etIncome = findViewById(R.id.inputIncome)
        etIncomeNote = findViewById(R.id.inputIncomeNote)
        inputIncomeDate = findViewById(R.id.inputIncomeDate)
        btnSaveIncome = findViewById(R.id.btnSaveIncome)

        etExpenses = findViewById(R.id.inputExpenses)
        etExpensesNote = findViewById(R.id.inputExpensesNote)
        inputExpensesDate = findViewById(R.id.inputExpensesDate)
        btnSaveExpenses = findViewById(R.id.btnSaveExpenses)

        etDisposableIncome = findViewById(R.id.inputDisposableIncome)
        btnSaveDisposableIncome = findViewById(R.id.btnSaveDisposableIncome)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)

        btnSaveWallet.setOnClickListener {
            saveWalletToFirestore()
        }

        btnSaveIncome.setOnClickListener {
            saveIncomeToFirestore()
        }

        btnSaveExpenses.setOnClickListener {
            saveExpensesToFirestore()
        }

        btnSaveDisposableIncome.setOnClickListener {
            saveDisposableIncomeToFirestore()
        }

        inputIncomeDate.setOnClickListener {
            showDatePicker { date ->
                selectedIncomeDate = date
                inputIncomeDate.setText(date)
            }
        }

        inputExpensesDate.setOnClickListener {
            showDatePicker { date ->
                selectedExpensesDate = date
                inputExpensesDate.setText(date)
            }
        }

        loadValuesFromFirestore()

        bottomNavigation.selectedItemId = R.id.nav_values
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_values -> true // Stay on this page
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

    private fun saveWalletToFirestore() {
        val wallet = etWallet.text.toString().toDoubleOrNull() ?: 0.0

        db.collection("budget").document("values")
            .update("wallet", wallet)
            .addOnSuccessListener {
                Toast.makeText(this, "Wallet amount saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving wallet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveIncomeToFirestore() {
        val income = etIncome.text.toString().toDoubleOrNull() ?: 0.0
        val note = etIncomeNote.text.toString()
        if (selectedIncomeDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val incomeData = hashMapOf(
            "amount" to income,
            "note" to note,
            "date" to selectedIncomeDate
        )

        db.collection("budget").document("values").collection("income").add(incomeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Income saved!", Toast.LENGTH_SHORT).show()
                etIncome.text.clear()
                etIncomeNote.text.clear()
                inputIncomeDate.text.clear()
                selectedIncomeDate = ""
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving income", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExpensesToFirestore() {
        val expenses = etExpenses.text.toString().toDoubleOrNull() ?: 0.0
        val note = etExpensesNote.text.toString()
        if (selectedExpensesDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val expensesData = hashMapOf(
            "amount" to expenses,
            "note" to note,
            "date" to selectedExpensesDate
        )

        db.collection("budget").document("values").collection("expenses").add(expensesData)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()
                etExpenses.text.clear()
                etExpensesNote.text.clear()
                inputExpensesDate.text.clear()
                selectedExpensesDate = ""
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDisposableIncomeToFirestore() {
        val disposableIncome = etDisposableIncome.text.toString().toDoubleOrNull() ?: 0.0

        db.collection("budget").document("values")
            .update("disposable_income", disposableIncome)
            .addOnSuccessListener {
                Toast.makeText(this, "Disposable Income saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving disposable income", Toast.LENGTH_SHORT).show()
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
                    etDisposableIncome.setText(
                        (document.get("disposable_income") as? Number)?.toDouble()?.toString() ?: "0"
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading values", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            callback(date)
        }, year, month, day)

        datePickerDialog.show()
    }
}
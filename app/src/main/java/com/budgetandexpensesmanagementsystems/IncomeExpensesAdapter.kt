package com.budgetandexpensesmanagementsystems

import android.content.*
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.*
import java.text.SimpleDateFormat
import java.util.*

data class IncomeExpenses(val type: String, val amount: Double, val note: String, val date: String)

class IncomeExpensesAdapter(private val context: Context, private val incomeExpense: List<IncomeExpenses>) :
    RecyclerView.Adapter<IncomeExpensesAdapter.IncomeExpensesViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    class IncomeExpensesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeExpensesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_income_expenses, parent, false)
        return IncomeExpensesViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeExpensesViewHolder, position: Int) {
        val incomeExpenses = incomeExpense[position]
        val currency = sharedPreferences.getString("currency", "USD($) ") ?: "USD($) "

        holder.tvType.text = incomeExpenses.type
        holder.tvAmount.text = "Amount: $currency${incomeExpenses.amount}"
        holder.tvNote.text = "Note: ${incomeExpenses.note}"

        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        if (notificationsEnabled) {
            scheduleNotification(context, incomeExpenses)
        }
    }

    override fun getItemCount(): Int = incomeExpense.size

    private fun scheduleNotification(context: Context, incomeExpenses: IncomeExpenses) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Upcoming ${incomeExpenses.type}")
            putExtra("message", "You have an upcoming ${incomeExpenses.type} of ${incomeExpenses.amount}")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            incomeExpenses.date.hashCode(), // Unique ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse the date and subtract 1 day
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transactionDate = dateFormat.parse(incomeExpenses.date)
        val notificationTime = Calendar.getInstance().apply {
            time = transactionDate!!
            add(Calendar.DAY_OF_YEAR, -1) // Schedule one day before
            set(Calendar.HOUR_OF_DAY, 9) // Notify at 9 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Set the alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, pendingIntent)
    }
}
package com.example.financetrackerapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.example.financetrackerapp.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class BudgetActivity : AppCompatActivity() {

    private lateinit var monthSpinner: Spinner
    private lateinit var budgetInput: EditText
    private lateinit var btnSave: Button
    private lateinit var barChart: BarChart
    private lateinit var warningText: TextView
    private lateinit var remainingBudgetText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private var selectedMonth: String = ""
    private var totalExpense: Double = 0.0
    private var budgetAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        //bind UI components
        monthSpinner = findViewById(R.id.monthSpinner)
        budgetInput = findViewById(R.id.budgetInput)
        btnSave = findViewById(R.id.saveBudgetButton)
        barChart = findViewById(R.id.budgetChart)
        warningText = findViewById(R.id.budgetWarning)
        remainingBudgetText = findViewById(R.id.remainingBudgetText)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        //manage UI functionality by each methods
        setupBottomNavigation()
        setupMonthSpinner()
        setupSaveButton()
        setupMonthSpinnerListener()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_budget
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transaction -> {
                    startActivity(Intent(this, TransactionActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budget -> {
                    bottomNavigation.selectedItemId = R.id.nav_budget
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMonthSpinner() {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        monthSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
    }

    //load budget to the page from relevant month
    private fun setupMonthSpinnerListener() {
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedMonth = parent?.getItemAtPosition(position).toString()
                // Load saved budget for this month
                val prefs = getSharedPreferences("budgets", MODE_PRIVATE)
                budgetAmount = prefs.getFloat(selectedMonth, 0f).toDouble()
                budgetInput.setText(if (budgetAmount > 0) budgetAmount.toString() else "")
                
                // Calculate expenses for selected month
                calculateExpenseForMonth(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    //load transaction list from SharedPreferences
    private fun calculateExpenseForMonth(month: String) {
        val sharedPreferences = getSharedPreferences("transactions", MODE_PRIVATE)
        val data = sharedPreferences.getString("transaction_list", null)
        totalExpense = 0.0
        if (data != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactionList: List<Transaction> = gson.fromJson(data, type)

            // Get the current year
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            // Format we expect: YYYY-MM-DD
            val monthNumber = getMonthNumber(month)
            val monthStr = String.format("%02d", monthNumber) // Pad with leading zero if needed
            val yearMonth = "$currentYear-$monthStr"

            for (transaction in transactionList) {
                // Check if transaction is an outcome and matches the selected month
                if (transaction.type == "Outcome" && transaction.date.startsWith(yearMonth)) {
                    totalExpense += transaction.amount
                }
            }
        }

        //call Update chart, remaining budget, warning
        updateRemainingBudget()
        updateChart()
        showWarning()
    }

    private fun getMonthNumber(monthName: String): Int {
        return when (monthName) {
            "January" -> 1
            "February" -> 2
            "March" -> 3
            "April" -> 4
            "May" -> 5
            "June" -> 6
            "July" -> 7
            "August" -> 8
            "September" -> 9
            "October" -> 10
            "November" -> 11
            "December" -> 12
            else -> 1
        }
    }

    //display remaining balance to the textView
    private fun updateRemainingBudget() {
        val remaining = budgetAmount - totalExpense
        remainingBudgetText.text = getString(R.string.remaining_budget, remaining)
        remainingBudgetText.visibility = android.view.View.VISIBLE
    }

    //validate budget inputs when click save button
    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val budgetStr = budgetInput.text.toString()
            if (budgetStr.isNotEmpty()) {
                val budget = budgetStr.toDoubleOrNull()
                if (budget != null && budget >= 0) {
                    selectedMonth = monthSpinner.selectedItem.toString()
                    budgetAmount = budget
                    calculateExpenseForMonth(selectedMonth)
                    updateChart()
                    updateRemainingBudget()
                    showWarning()
                    saveBudget(selectedMonth, budget)  //save budget in SharedPreferences
                } else {
                    Toast.makeText(this, getString(R.string.invalid_budget_amount), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, budgetAmount.toFloat()))
        entries.add(BarEntry(1f, totalExpense.toFloat()))

        val dataSet = BarDataSet(entries, getString(R.string.budget_vs_spent))
        dataSet.colors = listOf(ColorTemplate.COLORFUL_COLORS[0], ColorTemplate.COLORFUL_COLORS[1])
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = android.graphics.Color.BLACK
        
        val data = BarData(dataSet)
        data.barWidth = 0.4f
        data.setValueTextSize(16f)

        barChart.data = data
        
        // X-axis configuration
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf(getString(R.string.budget), getString(R.string.spent)))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 16f // Increased text size
        xAxis.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        xAxis.yOffset = 15f // Add space between axis and labels

        // Y-axis configuration
        val leftAxis = barChart.axisLeft
        leftAxis.textSize = 14f
        leftAxis.axisMinimum = 0f
        leftAxis.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        
        // Chart styling
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.textSize = 14f
        barChart.legend.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        barChart.legend.isEnabled = true
        barChart.setDrawValueAboveBar(true)
        barChart.setExtraBottomOffset(20f) // Add extra bottom spacing
        
        // Update remaining budget and warning text styles
        remainingBudgetText.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        warningText.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        
        barChart.animateY(500)
        barChart.invalidate()
    }

    private fun showWarning() {
        if (budgetAmount == 0.0) {
            warningText.text = getString(R.string.set_budget_first)
            warningText.setTextColor(getColor(android.R.color.holo_orange_light))
            warningText.visibility = android.view.View.VISIBLE
            return
        }

        val usagePercentage = (totalExpense / budgetAmount) * 100
        warningText.visibility = android.view.View.VISIBLE
        when {
            usagePercentage < 80 -> {
                warningText.text = getString(R.string.within_budget)
                warningText.setTextColor(getColor(R.color.green))
            }
            usagePercentage in 80.0..99.9 -> {
                warningText.text = getString(R.string.nearing_budget_limit)
                warningText.setTextColor(getColor(android.R.color.holo_orange_light))
            }
            else -> {
                warningText.text = getString(R.string.exceeded_budget)
                warningText.setTextColor(getColor(R.color.red))
            }
        }
    }

    //saved selected month budget in SharedPreferences
    private fun saveBudget(month: String, amount: Double) {
        val prefs: SharedPreferences = getSharedPreferences("budgets", MODE_PRIVATE)
        prefs.edit().putFloat(month, amount.toFloat()).apply()
    }
}

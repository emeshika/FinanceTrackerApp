package com.example.financetrackerapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import com.example.financetrackerapp.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HomeActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var incomeBarChart: BarChart
    private lateinit var expenseBarChart: BarChart
    private lateinit var totalBalanceText: TextView
    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView

    // Custom colors for charts (avoiding yellow and green)
    private val chartColors = intArrayOf(
        Color.rgb(233, 80, 80),    // Red
        Color.rgb(80, 80, 233),    // Blue
        Color.rgb(233, 80, 233),   // Purple
        Color.rgb(80, 233, 233),   // Cyan
        Color.rgb(233, 150, 80),   // Orange
        Color.rgb(150, 80, 233)    // Deep Purple
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        pieChart = findViewById(R.id.pieChart)
        incomeBarChart = findViewById(R.id.incomeBarChart)
        expenseBarChart = findViewById(R.id.expenseBarChart)
        totalBalanceText = findViewById(R.id.textTotalBalance)
        totalIncomeText = findViewById(R.id.textTotalIncome)
        totalExpenseText = findViewById(R.id.textTotalExpense)

        setupBarChart(incomeBarChart)
        setupBarChart(expenseBarChart)

        // Bottom navigation setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_transaction -> {
                    startActivity(Intent(this, TransactionActivity::class.java))
                    overridePendingTransition(0, 0) //disables the default transition animation
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    overridePendingTransition(0, 0)
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

    //setup bar chart
    private fun setupBarChart(chart: BarChart) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 14f
            }

            axisLeft.apply {
                setDrawGridLines(false)
                spaceTop = 35f
                axisMinimum = 0f
                textSize = 14f
            }

            axisRight.isEnabled = false
            legend.apply {
                isEnabled = true
                textSize = 14f
            }
        }
    }

    //Automatically refreshes data when adding new data
    override fun onResume() {
        super.onResume()
        loadTransactionData()
    }

    private fun loadTransactionData() {
        val sharedPreferences = getSharedPreferences("transactions", MODE_PRIVATE) // saved transaction list from SharedPreferences.
        val data = sharedPreferences.getString("transaction_list", null)  //Retrieves saved transaction list from SharedPreferences

        if (data != null) {
            val gson = Gson()
            val transactionList: List<Transaction> = gson.fromJson(data, object : TypeToken<List<Transaction>>() {}.type) //Converts the JSON string back to a list of Transaction objects

            var totalIncome = 0.0
            var totalExpense = 0.0
            val incomeCategoryMap = mutableMapOf<String, Double>()
            val expenseCategoryMap = mutableMapOf<String, Double>()

            // income and expenses, calculates totals, and groups by category.
            for (transaction in transactionList) {
                if (transaction.type == "Income") {
                    totalIncome += transaction.amount
                    val currentAmount = incomeCategoryMap.getOrDefault(transaction.category, 0.0)
                    incomeCategoryMap[transaction.category] = currentAmount + transaction.amount
                } else {
                    totalExpense += transaction.amount
                    val currentAmount = expenseCategoryMap.getOrDefault(transaction.category, 0.0)
                    expenseCategoryMap[transaction.category] = currentAmount + transaction.amount
                }
            }

            val balance = totalIncome - totalExpense

            // Update UI
            totalIncomeText.text = "Total Income: Rs. %.2f".format(totalIncome)
            totalExpenseText.text = "Total Expenses: Rs. %.2f".format(totalExpense)
            totalBalanceText.text = "Total Balance: Rs. %.2f".format(balance)

            updatePieChart(totalIncome, totalExpense)
            updateIncomeBarChart(incomeCategoryMap)
            updateExpenseBarChart(expenseCategoryMap)
        } else {
            // No transactions available
            totalIncomeText.text = "Total Income: Rs. 0.00"
            totalExpenseText.text = "Total Expenses: Rs. 0.00"
            totalBalanceText.text = "Total Balance: Rs. 0.00"
            updatePieChart(0.0, 0.0)
            updateIncomeBarChart(emptyMap())
            updateExpenseBarChart(emptyMap())
        }
    }

    //call updateBarChart() with the correct chart and label.
    private fun updateIncomeBarChart(categoryMap: Map<String, Double>) {
        updateBarChart(incomeBarChart, categoryMap, "Income by Category")
    }

    private fun updateExpenseBarChart(categoryMap: Map<String, Double>) {
        updateBarChart(expenseBarChart, categoryMap, "Expense by Category")
    }

    private fun updateBarChart(chart: BarChart, categoryMap: Map<String, Double>, label: String) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        categoryMap.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        val dataSet = BarDataSet(entries, label)
        dataSet.colors = chartColors.toList()
        dataSet.valueTextSize = 16f

        val barData = BarData(dataSet)
        barData.setValueTextSize(16f)
        chart.data = barData
        
        // Set X-axis labels
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelRotationAngle = -45f

        // Refresh the chart
        chart.invalidate()
    }

    private fun updatePieChart(income: Double, expenses: Double) {
        val entries = ArrayList<PieEntry>()
        if (income > 0) entries.add(PieEntry(income.toFloat(), "Income"))
        if (expenses > 0) entries.add(PieEntry(expenses.toFloat(), "Expenses"))

        val dataSet = PieDataSet(entries, "")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        dataSet.valueTextSize = 20f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.textSize = 16f
        pieChart.legend.isEnabled = true
        pieChart.setExtraOffsets(8f, 8f, 8f, 8f)
        pieChart.invalidate()
    }
}

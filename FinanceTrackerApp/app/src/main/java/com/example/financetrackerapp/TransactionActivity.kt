package com.example.financetrackerapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.financetrackerapp.model.Transaction
import com.example.financetrackerapp.adapter.TransactionAdapter
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionActivity : AppCompatActivity(), TransactionAdapter.OnTransactionActionListener {

    private lateinit var titleInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var rbIncome: RadioButton
    private lateinit var rbOutcome: RadioButton
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var transactionRecycler: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    private var transactions: MutableList<Transaction> = mutableListOf()
    private lateinit var adapter: TransactionAdapter
    private var isEditing = false
    private var editPosition = -1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        titleInput = findViewById(R.id.editTextTitle)
        amountInput = findViewById(R.id.editTextAmount)
        dateInput = findViewById(R.id.editTextDate)
        rgTransactionType = findViewById(R.id.rgTransactionType)
        rbIncome = findViewById(R.id.rbIncome)
        rbOutcome = findViewById(R.id.rbOutcome)
        categorySpinner = findViewById(R.id.spinnerCategory)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)
        transactionRecycler = findViewById(R.id.recyclerViewTransactions)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupSpinners()
        setupDatePicker()
        loadTransactionsFromPreferences()
        setupRecyclerView()
        setupBottomNavigation()

        saveButton.setOnClickListener {
            if (validateInputs()) {
                val title = titleInput.text.toString()
                val amount = amountInput.text.toString().toDouble()
                val type = if (rbIncome.isChecked) "Income" else "Outcome"
                val category = categorySpinner.selectedItem.toString()
                val date = dateInput.text.toString()
                val transaction = Transaction(
                    id = if (isEditing) transactions[editPosition].id else (transactions.maxOfOrNull { it.id } ?: 0) + 1,
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date
                )

                if (isEditing && editPosition != -1) {
                    transactions[editPosition] = transaction
                    Toast.makeText(this, "Transaction Updated", Toast.LENGTH_SHORT).show()
                    isEditing = false
                    editPosition = -1
                } else {
                    transactions.add(transaction)
                    Toast.makeText(this, "Transaction Added", Toast.LENGTH_SHORT).show()
                }

                adapter.notifyDataSetChanged()
                saveTransactionListToPreferences()
                clearInputs()
            }
        }

        cancelButton.setOnClickListener {
            clearInputs()
            isEditing = false
            editPosition = -1
        }
    }

    private fun setupSpinners() {
        val categories = arrayOf("Insurance", "Salary", "Bills", "Tax", "Rent", "Groceries", "Interest", "Other")
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    dateInput.setText(dateFormat.format(selectedDate.time))
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(transactions, this)
        transactionRecycler.layoutManager = LinearLayoutManager(this)
        transactionRecycler.adapter = adapter
    }

    fun getExpensesForMonth(month: Int): List<Transaction> {
        return transactions.filter { transaction ->
            transaction.type.equals("Outcome", ignoreCase = true) && extractMonthFromDate(transaction.date) == month
        }
    }

    fun extractMonthFromDate(date: String): Int {
        // Assumes date format "yyyy-MM-dd"
        val parts = date.split("-")
        return parts[1].toInt()  // Extracts the month from the date string
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_transaction
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transaction -> true
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

    //setup validations
    private fun validateInputs(): Boolean {
        val title = titleInput.text.toString()
        val amount = amountInput.text.toString()
        val date = dateInput.text.toString()

        if (TextUtils.isEmpty(title) || !title.matches(Regex("^[a-zA-Z0-9 ]+$"))) {
            titleInput.error = "Enter valid title (letters and numbers only)"
            return false
        }

        if (TextUtils.isEmpty(amount) || !amount.matches(Regex("^\\d+(\\.\\d+)?$"))) {
            amountInput.error = "Enter valid amount (numbers only)"
            return false
        }

        if (TextUtils.isEmpty(date)) {
            dateInput.error = "Please select a date"
            return false
        }

        if (!rbIncome.isChecked && !rbOutcome.isChecked) {
            Toast.makeText(this, "Select transaction type", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun clearInputs() {
        titleInput.text.clear()
        amountInput.text.clear()
        dateInput.text.clear()
        rgTransactionType.clearCheck()
        categorySpinner.setSelection(0)
        saveButton.text = "Save"
    }

    override fun onEdit(position: Int) {
        val transaction = transactions[position]
        titleInput.setText(transaction.title)
        amountInput.setText(transaction.amount.toString())
        dateInput.setText(transaction.date)
        if (transaction.type == "Income") rbIncome.isChecked = true else rbOutcome.isChecked = true
        categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(transaction.category))

        isEditing = true
        editPosition = position
        saveButton.text = "Update"
    }

    override fun onDelete(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("OK") { _, _ ->
                transactions.removeAt(position)
                adapter.notifyDataSetChanged()
                saveTransactionListToPreferences()
                Toast.makeText(this, "Transaction Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveTransactionListToPreferences() {
        val sharedPreferences = getSharedPreferences("transactions", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(transactions)
        editor.putString("transaction_list", json)
        editor.apply()
    }

    private fun loadTransactionsFromPreferences() {
        val sharedPreferences = getSharedPreferences("transactions", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("transaction_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            transactions = gson.fromJson(json, type) ?: mutableListOf()
        }
    }
}

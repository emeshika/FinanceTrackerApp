package com.example.financetrackerapp.model

// Gson-compatible data class -- core model of handle all transaction data
data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String,
    val date: String // Format: "dd-MM-yyyy"
)


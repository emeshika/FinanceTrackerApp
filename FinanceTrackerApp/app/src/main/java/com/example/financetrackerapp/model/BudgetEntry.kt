package com.example.financetrackerapp.model

//passing budget entries
data class BudgetEntry(
    val date: String,
    val reason: String,
    val amount: Double,
    val type: String
)
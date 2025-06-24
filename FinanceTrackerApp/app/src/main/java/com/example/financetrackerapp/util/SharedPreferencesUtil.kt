package com.example.financetrackerapp.util

import android.content.Context
import android.content.SharedPreferences
import com.example.financetrackerapp.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPreferencesUtil {
    private const val PREFS_NAME = "transactions_prefs"
    private const val KEY_TRANSACTIONS = "transactions"

    fun saveTransaction(context: Context, transaction: Transaction) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get existing transactions list, or create a new one if it doesn't exist
        val existingTransactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val existingTransactions = Gson().fromJson(existingTransactionsJson, Array<Transaction>::class.java).toMutableList()

        // Add new transaction
        existingTransactions.add(transaction)

        // Save back the updated list to SharedPreferences
        val updatedTransactionsJson = Gson().toJson(existingTransactions)
        editor.putString(KEY_TRANSACTIONS, updatedTransactionsJson)
        editor.apply()
    }

    //retrive all saved transaction list
    fun getTransactions(context: Context): List<Transaction> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        return Gson().fromJson(transactionsJson, Array<Transaction>::class.java).toList()
    }

    //delete one transaction
    fun removeTransaction(context: Context, transaction: Transaction) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get existing transactions list
        val existingTransactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val existingTransactions = Gson().fromJson(existingTransactionsJson, Array<Transaction>::class.java).toMutableList()

        // Remove the transaction
        existingTransactions.remove(transaction)

        // Save back the updated list to SharedPreferences
        val updatedTransactionsJson = Gson().toJson(existingTransactions)
        editor.putString(KEY_TRANSACTIONS, updatedTransactionsJson)
        editor.apply()
    }

    //update one of transaction in the transaction list
    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get existing transactions list
        val existingTransactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val existingTransactions = Gson().fromJson(existingTransactionsJson, Array<Transaction>::class.java).toMutableList()

        // Find the index of the transaction to update
        val index = existingTransactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            existingTransactions[index] = updatedTransaction
        }

        // Save back the updated list to SharedPreferences
        val updatedTransactionsJson = Gson().toJson(existingTransactions)
        editor.putString(KEY_TRANSACTIONS, updatedTransactionsJson)
        editor.apply()
    }
}

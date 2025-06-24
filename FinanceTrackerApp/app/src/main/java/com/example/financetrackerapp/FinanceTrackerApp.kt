package com.example.financetrackerapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

//set theme automatically as user preferences
class FinanceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize theme based on saved SharedPreference
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        //apply theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
} 
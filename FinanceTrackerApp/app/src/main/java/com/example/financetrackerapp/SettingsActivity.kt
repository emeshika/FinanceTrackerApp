package com.example.financetrackerapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    //Declares views
    private lateinit var currencySpinner: Spinner
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var backupButton: Button
    private lateinit var restoreButton: Button
    private lateinit var themeSwitch: SwitchCompat
    private lateinit var logoutButton: Button

    private val CHANNEL_ID = "backup_notification_channel"
    private val NOTIFICATION_ID = 1

    // Available currency options
    private val currencies = arrayOf("LKR", "USD", "EUR", "GBP", "INR", "JPY")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        currencySpinner = findViewById(R.id.spinnerCurrency)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        backupButton = findViewById(R.id.backupButton)
        restoreButton = findViewById(R.id.restoreButton)
        themeSwitch = findViewById(R.id.themeSwitch)
        logoutButton = findViewById(R.id.logoutButton)

        // Setup methods
        createNotificationChannel()
        setupCurrencySpinner()
        setupBottomNavigation()
        setupBackupButton()
        setupRestoreButton()
        setupThemeSwitch()
        setupLogoutButton()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.backup_notification_channel_name)
            val descriptionText = getString(R.string.backup_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showBackupNotification(success: Boolean, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(if (success) R.drawable.ic_backup_success else R.drawable.ic_backup_failed)
            .setContentTitle(if (success) getString(R.string.backup_successful) else getString(R.string.backup_failed))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
                return
            }
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.could_not_show_notification, e.message), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            backupData()
        }
    }

    private fun setupBackupButton() {
        backupButton.setOnClickListener {
            backupData()
        }
    }

    private fun backupData() {
        try {
            // Create backup directory if it doesn't exist
            val backupDir = File(getExternalFilesDir(null), "FinanceTrackerBackup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Create timestamp for the backup file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.txt")

            // Get data from SharedPreferences
            val transactionData = getSharedPreferences("transactions", MODE_PRIVATE).all
            val budgetData = getSharedPreferences("budgets", MODE_PRIVATE).all
            val settingsData = getSharedPreferences("Settings", MODE_PRIVATE).all

            // Write data to backup file
            FileOutputStream(backupFile).use { fos ->
                fos.write("=== FINANCE TRACKER BACKUP ===\n".toByteArray())
                fos.write(
                    "Date: ${
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                    }\n\n".toByteArray()
                )

                fos.write("=== TRANSACTIONS ===\n".toByteArray())
                transactionData.forEach { (key, value) ->
                    fos.write("$key: $value\n".toByteArray())
                }

                fos.write("\n=== BUDGETS ===\n".toByteArray())
                budgetData.forEach { (key, value) ->
                    fos.write("$key: $value\n".toByteArray())
                }

                fos.write("\n=== SETTINGS ===\n".toByteArray())
                settingsData.forEach { (key, value) ->
                    fos.write("$key: $value\n".toByteArray())
                }
            }

            // Show success notification
            showBackupNotification(true, "Data backup completed")

        } catch (e: Exception) {
            // Show error notification
            showBackupNotification(false, "Backup failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
        currencySpinner.adapter = adapter

        // Load saved currency from SharedPreferences
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val savedCurrency = prefs.getString("currency", "LKR")
        val pos = currencies.indexOf(savedCurrency)
        if (pos != -1) currencySpinner.setSelection(pos)

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                val selected = currencies[position]
                val editor = prefs.edit()
                editor.putString("currency", selected)
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_settings
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
                    startActivity(Intent(this, BudgetActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> {
                    bottomNavigation.selectedItemId = R.id.nav_settings
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRestoreButton() {
        restoreButton.setOnClickListener {
            showRestoreDialog()
        }
    }

    private fun showRestoreDialog() {
        val backupDir = File(getExternalFilesDir(null), "FinanceTrackerBackup")
        if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() == true) {
            Toast.makeText(this, getString(R.string.no_backup_files_found), Toast.LENGTH_SHORT).show()
            return
        }

        val backupFiles = backupDir.listFiles()?.filter { it.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
        if (backupFiles.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.no_valid_backup_files), Toast.LENGTH_SHORT).show()
            return
        }

        val fileNames = backupFiles.map {
            val date = Date(it.lastModified())
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            getString(R.string.backup_from, formatter.format(date))
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_backup_to_restore))
            .setItems(fileNames) { _, which ->
                val selectedFile = backupFiles[which]
                confirmRestore(selectedFile)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmRestore(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_restore))
            .setMessage(getString(R.string.restore_confirmation_message))
            .setPositiveButton(getString(R.string.restore)) { _, _ ->
                restoreData(backupFile)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun restoreData(backupFile: File) {
        try {
            val transactionPrefs = getSharedPreferences("transactions", MODE_PRIVATE).edit()
            val budgetPrefs = getSharedPreferences("budgets", MODE_PRIVATE).edit()
            val settingsPrefs = getSharedPreferences("Settings", MODE_PRIVATE).edit()

            // Clear existing data
            transactionPrefs.clear()
            budgetPrefs.clear()
            settingsPrefs.clear()

            var currentSection = ""
            backupFile.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    when {
                        line.contains("=== TRANSACTIONS ===") -> currentSection = "transactions"
                        line.contains("=== BUDGETS ===") -> currentSection = "budgets"
                        line.contains("=== SETTINGS ===") -> currentSection = "settings"
                        line.contains(": ") -> {
                            val (key, value) = line.split(": ", limit = 2)
                            when (currentSection) {
                                "transactions" -> transactionPrefs.putString(key, value)
                                "budgets" -> budgetPrefs.putString(key, value)
                                "settings" -> settingsPrefs.putString(key, value)
                            }
                        }
                    }
                }
            }

            // Apply all changes
            transactionPrefs.apply()
            budgetPrefs.apply()
            settingsPrefs.apply()

            // Show success notification
            showBackupNotification(true, getString(R.string.data_restored_successfully))

            // Restart app to apply restored settings
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finishAffinity()

        } catch (e: Exception) {
            showBackupNotification(false, getString(R.string.restore_failed, e.message))
            e.printStackTrace()
        }
    }

    private fun setupThemeSwitch() {
        // Get current theme mode from SharedPreferences
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        // Set initial switch state
        themeSwitch.isChecked = isDarkMode

        // Set up switch listener
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save theme preference
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            // Apply theme immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Restart activity to apply theme changes
            recreate()
        }
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_logout))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun performLogout() {
        // Clear login status
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            putString("current_user", null)
            apply()
        }

        // Navigate to MainActivity and clear back stack
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                backupData()
                true
            }

            R.id.action_restore -> {
                showRestoreDialog()
                true
            }

            R.id.action_theme -> {
                showThemeDialog()
                true
            }

            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showThemeDialog() {
        val currentTheme = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.theme_settings))
            .setMessage(getString(R.string.choose_preferred_theme))
            .setPositiveButton(getString(R.string.dark_theme)) { _, _ ->
                // Save and apply dark theme
                getSharedPreferences("Settings", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", true)
                    .apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                themeSwitch.isChecked = true
                recreate()
            }
            .setNegativeButton(getString(R.string.light_theme)) { _, _ ->
                // Save and apply light theme
                getSharedPreferences("Settings", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", false)
                    .apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                themeSwitch.isChecked = false
                recreate()
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }
}

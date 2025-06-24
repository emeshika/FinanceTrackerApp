package com.example.financetrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //get references to the UI components
        val nameField = findViewById<EditText>(R.id.editTextName)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val signupLink = findViewById<TextView>(R.id.textSignup)

        loginButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val password = passwordField.text.toString()

            // Validate inputs
            when {
                name.isEmpty() -> {
                    nameField.error = getString(R.string.email_empty_error)
                    nameField.requestFocus()
                }
                !name.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")) -> {
                    nameField.error = getString(R.string.email_invalid_error)
                    nameField.requestFocus()
                }

                password.isEmpty() -> {
                    passwordField.error = getString(R.string.password_empty_error)
                    passwordField.requestFocus()
                }
                password.length < 8 -> {
                    passwordField.error = getString(R.string.password_length_error)
                    passwordField.requestFocus()
                }
                else -> {
                    // If validation passes, navigate to HomeActivity
                    if (name.isNotEmpty() && password.isNotEmpty()) {
                        //  Navigate to HomeActivity
                        val loginButton = findViewById<Button>(R.id.buttonLogin)
                        loginButton.setOnClickListener {
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.credentials_empty_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}

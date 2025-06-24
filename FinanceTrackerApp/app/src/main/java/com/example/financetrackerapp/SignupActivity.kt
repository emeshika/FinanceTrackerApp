package com.example.financetrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameField = findViewById<EditText>(R.id.editTextSignupName)
        val passwordField = findViewById<EditText>(R.id.editTextSignupPassword)
        val signupButton = findViewById<Button>(R.id.buttonSignup)

        signupButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val password = passwordField.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (!name.matches(Regex("^[a-zA-Z][a-zA-Z ]*[a-zA-Z]\$"))) {
                Toast.makeText(this, "Name must contain only letters and spaces, and cannot start or end with a space", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("username", name)
                startActivity(intent)
                finish()
            }
        }
    }
}

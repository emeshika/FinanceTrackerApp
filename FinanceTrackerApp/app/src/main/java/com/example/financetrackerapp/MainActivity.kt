package com.example.financetrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val signupBtn = findViewById<Button>(R.id.btnSignup)

        
        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}

package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fakebook.SocialMediaApp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityLoginBinding

    // View components
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize view components
        btnRegister = binding.btnRegister

        btnRegister.setOnClickListener {

            // Navigate to UserEmailPasswordActivity
            val intent = Intent(this, UserEmailPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
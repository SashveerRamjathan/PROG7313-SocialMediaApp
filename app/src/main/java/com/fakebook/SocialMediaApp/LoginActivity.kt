package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fakebook.SocialMediaApp.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    // View Binding
    private lateinit var binding: ActivityLoginBinding

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // View components
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: TextView // Initializing register as a Button (even though it is a TextView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Removed functionality as it messes with the padding
        // ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //     insets
        // }

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize view components
        etEmail = binding.etEmail
        etPassword = binding.etPassword
        btnLogin = binding.btnLogin
        btnRegister = binding.tvRegister

        // On click listener for the login button
        btnLogin.setOnClickListener {
            // Get email and password from edit texts
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Error handling
            // Email validation
            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            // Password validation
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Attempt to sign in with email and password
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Display success message
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Show error message if login fails
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // On click listener for the register button
        btnRegister.setOnClickListener {
            // Navigate to UserEmailPasswordActivity
            val intent = Intent(this, UserEmailPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
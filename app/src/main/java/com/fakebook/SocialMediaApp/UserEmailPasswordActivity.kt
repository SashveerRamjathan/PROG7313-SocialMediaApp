package com.fakebook.SocialMediaApp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fakebook.SocialMediaApp.databinding.ActivityUserEmailPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class UserEmailPasswordActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityUserEmailPasswordBinding

    // View Components
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnNext: Button
    private lateinit var btnLogin: Button

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserEmailPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize view components
        etEmail = binding.etEmail
        etPassword = binding.etPassword
        etConfirmPassword = binding.etConfirmPassword
        btnNext = binding.btnNext
        btnLogin = binding.btnLogin

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        btnNext.setOnClickListener {

            // get values from fields
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            //check if all fields are filled
            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty())
            {
                // check if email is valid (contains "@" and 1 "." after "@")
                if (!email.contains("@") || !email.contains("."))
                {
                    etEmail.error = "Email is invalid"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }

                // check if ("." after "@" is not the last character)
                if (email.lastIndexOf(".") < email.lastIndexOf("@"))
                {
                    etEmail.error = "Email is invalid"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }

                // check if password is valid (at least 6 characters)
                if (password.length < 6)
                {
                    etPassword.error = "Password must be at least 6 characters"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }


                // check if password and confirm password match
                if (password == confirmPassword)
                {

                    // navigate to next activity to create user profile
                    val intent = Intent(this, CreateUserProfileActivity::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("password", password)
                    startActivity(intent)
                }
                else
                {
                    // show error message in text fields
                    etPassword.error = "Passwords do not match"
                    etPassword.requestFocus()
                    etConfirmPassword.error = "Passwords do not match"
                    etConfirmPassword.requestFocus()

                    return@setOnClickListener
                }
            }
            else
            {
                // show error message
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // navigate to login activity
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
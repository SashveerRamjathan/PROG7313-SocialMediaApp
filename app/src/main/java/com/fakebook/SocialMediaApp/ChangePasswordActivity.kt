package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fakebook.SocialMediaApp.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChangePasswordActivity : AppCompatActivity() {
    // View binding
    private lateinit var binding: ActivityChangePasswordBinding

    // View components
    private lateinit var btnBack: ImageButton
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize view components
        btnBack = binding.btnBack
        etCurrentPassword = binding.etCurrentPassword
        etNewPassword = binding.etNewPassword

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Set up click listeners
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdateProfile.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()

            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {

                if (newPassword.length < 6) {
                    etNewPassword.error = "New password must be at least 6 characters"
                    etNewPassword.requestFocus()
                    return@setOnClickListener
                }

                if (currentPassword == newPassword) {
                    etNewPassword.error = "New password cannot be the same as current password"
                    etNewPassword.requestFocus()
                    return@setOnClickListener
                }

                val authUser = auth.currentUser
                if (authUser?.email != null) {
                    reauthenticateAndUpdate(authUser, newPassword)
                } else {
                    Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                }
            }
        }
    }

    private fun reauthenticateAndUpdate(
        authUser: FirebaseUser,
        newPassword: String
    ) {
        binding.btnUpdateProfile.isEnabled = false

        val currentPassword = etCurrentPassword.text.toString()
        val credential = EmailAuthProvider.getCredential(authUser.email!!, currentPassword)

        lifecycleScope.launch {
            try {
                // Step 1: Re-authenticate
                authUser.reauthenticate(credential).await()

                // Step 2: Update password
                updatePassword(authUser, newPassword)

                Toast.makeText(
                    this@ChangePasswordActivity,
                    "Password updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                redirectToLogin()
            } catch (e: FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_WRONG_PASSWORD" -> {
                        etCurrentPassword.error = "Current password is incorrect"
                        etCurrentPassword.requestFocus()
                    }
                    "ERROR_USER_NOT_FOUND" -> {
                        Toast.makeText(this@ChangePasswordActivity, "No such user found", Toast.LENGTH_SHORT).show()
                    }
                    "ERROR_NETWORK_REQUEST_FAILED" -> {
                        Toast.makeText(this@ChangePasswordActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Catch any other FirebaseAuthException errors
                        Toast.makeText(this@ChangePasswordActivity, "Authentication failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }


    private suspend fun updatePassword(user: FirebaseUser, newPassword: String) {
        try {
            user.updatePassword(newPassword).await()
            Log.d("ChangePasswordActivity", "Password updated successfully")
        } catch (e: Exception) {
            Log.e("ChangePasswordActivity", "Password update failed: ${e.message}")
            Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}


package com.fakebook.SocialMediaApp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fakebook.SocialMediaApp.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    // View binding
    private lateinit var binding: ActivitySplashScreenBinding

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val prefs = getSharedPreferences("MODE", MODE_PRIVATE)
        val isNight = prefs.getBoolean("night", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNight)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Delay the redirection by 3 seconds
        Handler(Looper.getMainLooper()).postDelayed(
            {
                // Check if user is already logged in
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    // User is already logged in, navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // User is not logged in, navigate to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }, 3000 // 3000 milliseconds (3 seconds) delay
        )
    }
}
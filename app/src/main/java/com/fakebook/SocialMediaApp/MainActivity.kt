package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fakebook.SocialMediaApp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityMainBinding

    // View components
    private lateinit var btnLogout: Button
    private lateinit var bnvNavbar: BottomNavigationView

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Initialize view components
        btnLogout = binding.btnLogout
        bnvNavbar = binding.bnvNavbar

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Highlight the Home menu item
        bnvNavbar.menu.findItem(R.id.miHome).isChecked = true

        btnLogout.setOnClickListener {

            auth.signOut()

            // check if user is signed out
            if (auth.currentUser == null) {
                // display message
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

                // redirect to login activity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                // display error message
                Toast.makeText(this, "Error signing out", Toast.LENGTH_SHORT).show()

                // log the error
                Log.d("MainActivity", "Error signing out: ${auth.currentUser?.email}")
            }
        }

        // Set up Bottom Navigation View onClickListener
        bnvNavbar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.miHome -> true

                R.id.miPost -> {

                    // navigate to create post activity
                    startActivity(Intent(this, CreatePostActivity::class.java))
                    finish()
                    true
                }

                R.id.miProfile -> {

                    // display coming soon toast
                    Toast.makeText(this, "Profile Feature - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

    }
}
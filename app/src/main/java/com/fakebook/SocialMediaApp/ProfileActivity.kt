package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fakebook.SocialMediaApp.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity()
{
    // view binding
    private lateinit var binding: ActivityProfileBinding

    // firebase auth
    private lateinit var auth: FirebaseAuth

    //View Components
    private lateinit var btnLogout: Button
    private lateinit var bnvNavbar: BottomNavigationView
    private lateinit var btnSettings: ImageButton


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize View Components
        btnLogout = binding.btnLogout
        bnvNavbar = binding.bnvNavbar
        btnSettings = binding.btnSettings

        // Highlight the Profile menu item
        bnvNavbar.menu.findItem(R.id.miProfile).isChecked = true

        // Set up OnClickListener logout
        setUpOnClickListener()
    }

    private fun setUpOnClickListener()
    {
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
                R.id.miHome -> {

                    // navigate to home activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                R.id.miPost -> {

                    // navigate to create post activity
                    startActivity(Intent(this, CreatePostActivity::class.java))
                    finish()
                    true
                }

                R.id.miProfile -> true

                else -> false
            }
        }
    }
}
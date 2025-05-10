package com.fakebook.SocialMediaApp

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.adapters.ProfileAdapter
import com.fakebook.SocialMediaApp.databinding.ActivityProfileBinding
import com.fakebook.SocialMediaApp.models.Post
import com.fakebook.SocialMediaApp.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    // region Declarations
    // View Binding
    private lateinit var binding: ActivityProfileBinding

    // Firebase
    // Authentication
    private lateinit var auth: FirebaseAuth

    // FireStore
    private lateinit var firestore: FirebaseFirestore

    //View Components
    private lateinit var btnSettings: ImageButton
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvBio: TextView
    private lateinit var rvProfilePosts: RecyclerView
    private lateinit var bnvNavbar: BottomNavigationView

    // Themes
    private val sharedPreferences: SharedPreferences by lazy { getSharedPreferences("MODE", MODE_PRIVATE) }
    private val editor: SharedPreferences.Editor by lazy { sharedPreferences.edit() }
    // endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialise
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // View Binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase
        // Authentication
        auth = FirebaseAuth.getInstance()
        // FireStore
        firestore = FirebaseFirestore.getInstance()

        // Initialize View Components
        btnSettings = binding.btnSettings
        ivProfilePicture = binding.ivProfilePicture
        tvUsername = binding.tvUsername
        tvFullName = binding.tvFullName
        tvBio = binding.tvBio
        rvProfilePosts = binding.rvProfilePosts
        bnvNavbar = binding.bnvNavbar

        // Set up the RecyclerView with GridLayoutManager (e.g., 3 columns)
        val gridLayoutManager = GridLayoutManager(this, 3)
        rvProfilePosts.layoutManager = gridLayoutManager
        val profilePostAdapter = ProfileAdapter(listOf())
        rvProfilePosts.adapter = profilePostAdapter

        // Highlight the Profile menu item
        bnvNavbar.menu.findItem(R.id.miProfile).isChecked = true

        // Load user profile info and posts
        loadUserProfile()
        loadUserPosts(profilePostAdapter)

        // Set up OnClickListener logout
        setUpOnClickListener()
    }

    private fun loadUserProfile() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let {
                    // Populate your header views
                    tvUsername.text = it.username
                    tvFullName.text = it.fullName
                    tvBio.text = it.bio
                    Picasso.get()
                        .load(it.profilePictureLink)
                        .placeholder(R.drawable.ic_default_profile)
                        .into(ivProfilePicture)
                }
            }
    }

    private fun loadUserPosts(adapter: ProfileAdapter) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val posts = querySnapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                adapter.updatePosts(posts)
            }
            .addOnFailureListener {
                Log.e("ProfileActivity", "Error loading user posts", it)
            }
    }

    private fun setUpOnClickListener() {
        btnSettings.setOnClickListener {
            // Show Dialogue
            val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            // access views from dialog
            val switchDarkMode = dialogView.findViewById<SwitchMaterial>(R.id.switchDarkMode)
            val btnChangePassword = dialogView.findViewById<Button>(R.id.btnChangePassword)
            val btnUpdateProfileInfo = dialogView.findViewById<Button>(R.id.btnUpdateProfileInfo)
            val btnSignOut = dialogView.findViewById<Button>(R.id.btnSignOut)

            // Set switch state based on shared preferences
            switchDarkMode.isChecked = sharedPreferences.getBoolean("night", false)

            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                editor.putBoolean("night", isChecked).apply()
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked)
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            btnUpdateProfileInfo.setOnClickListener {

                // navigate to update account activity
                startActivity(Intent(this, UpdateProfileActivity::class.java))
            }

            btnChangePassword.setOnClickListener {

                // navigate to update account activity
                startActivity(Intent(this, ChangePasswordActivity::class.java))
            }

            btnSignOut.setOnClickListener {
                val logoutDialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
                val logoutDialog = AlertDialog.Builder(this)
                    .setView(logoutDialogView)
                    .create()
                logoutDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                logoutDialog.show()

                // access views from logout dialog
                val btnLogOut = logoutDialogView.findViewById<Button>(R.id.btnSignOut)
                val btnCancel = logoutDialogView.findViewById<Button>(R.id.btnCancel)

                btnLogOut.setOnClickListener {
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

                btnCancel.setOnClickListener {

                    dialog.dismiss()
                    logoutDialog.dismiss()
                }
            }
        }

        // Set up Bottom Navigation View onClickListener
        bnvNavbar.setOnItemSelectedListener {
            when (it.itemId) {
                // Navigate to Home Activity
                R.id.miHome -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                R.id.miProfile -> true

                else -> false
            }
        }
    }
}
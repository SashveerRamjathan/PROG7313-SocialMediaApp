package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.DataModels.Post
import com.fakebook.SocialMediaApp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityMainBinding

    // View components
    private lateinit var btnLogout: Button
    private lateinit var bnvNavbar: BottomNavigationView
    private lateinit var rvPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Firebase FireStore
    private lateinit var firestore: FirebaseFirestore

    // post notification request code
    private val REQUEST_CODE_POST_NOTIFICATIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }


        // Initialize view components
        btnLogout = binding.btnLogout
        bnvNavbar = binding.bnvNavbar
        rvPosts = binding.rvPosts

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

        // Set up RecyclerView
        rvPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(listOf())
        rvPosts.adapter = postAdapter

        // Load Posts from FireStore
        loadPosts()
    }

    private fun loadPosts() {
        // Query FireStore collection "posts" ordered by timestamp descending
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val posts = mutableListOf<Post>()
                for (document in querySnapshot.documents) {
                    document.toObject(Post::class.java)?.let { posts.add(it) }
                }
                // Update adapter with new list
                postAdapter.updatePosts(posts)
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error loading posts", exception)
                Snackbar.make(
                    findViewById(R.id.main),
                    "Failed to load posts",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }
}
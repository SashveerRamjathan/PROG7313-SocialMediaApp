package com.fakebook.SocialMediaApp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.adapters.PostAdapter
import com.fakebook.SocialMediaApp.databinding.ActivityMainBinding
import com.fakebook.SocialMediaApp.models.Post
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    // region Declaration
    // View Binding
    private lateinit var binding: ActivityMainBinding

    // View components
    private lateinit var bnvNavbar: BottomNavigationView
    private lateinit var rvPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Firebase FireStore
    private lateinit var firestore: FirebaseFirestore
    // endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // Initialize View Components
        bnvNavbar = binding.bnvNavbar
        rvPosts = binding.rvPosts

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Highlight the Home menu item
        bnvNavbar.menu.findItem(R.id.miHome).isChecked = true

        // Set up RecyclerView
        rvPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(listOf())
        rvPosts.adapter = postAdapter

        // Set up OnClickListener
        setUpOnClickListener()

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

    private fun setUpOnClickListener() {

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

                    // navigate to profile activity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}
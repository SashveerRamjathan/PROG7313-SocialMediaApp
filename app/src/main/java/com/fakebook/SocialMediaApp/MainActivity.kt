package com.fakebook.SocialMediaApp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fakebook.SocialMediaApp.adapters.PostAdapter
import com.fakebook.SocialMediaApp.databinding.ActivityMainBinding
import com.fakebook.SocialMediaApp.helpers.EngagementUtils
import com.fakebook.SocialMediaApp.helpers.RankingUtils
import com.fakebook.SocialMediaApp.models.Post
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    // region Declaration
    // View Binding
    private lateinit var binding: ActivityMainBinding

    // View components
    private lateinit var bnvNavbar: BottomNavigationView
    private lateinit var rvPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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
        swipeRefreshLayout = binding.swipeRefresh

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

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadPosts()
    {
        lifecycleScope.launch {
            try
            {
                val querySnapshot = firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val posts = querySnapshot.documents.mapNotNull { it.toObject(Post::class.java) }

                // Attach engagement (likes/comments)
                val enrichedPosts = EngagementUtils.attachEngagementCountsToPosts(posts)

                // Rank posts by score
                val rankedPosts = RankingUtils.rankPosts(enrichedPosts)

                postAdapter.updatePosts(rankedPosts)

            }
            catch (e: Exception)
            {
                Log.e("MainActivity", "Error loading posts", e)

                Snackbar.make(
                    findViewById(R.id.main),
                    "Failed to load posts",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setUpOnClickListener() {
        // Set Up FAB onClickListener
        binding.fabPost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        // Set up Bottom Navigation View onClickListener
        bnvNavbar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.miHome -> true

                R.id.miSearch -> {

                    // navigate to search activity
                    startActivity(Intent(this, PostSearchActivity::class.java))
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
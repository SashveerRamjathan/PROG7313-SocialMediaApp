package com.fakebook.SocialMediaApp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakebook.SocialMediaApp.adapters.ProfileAdapter
import com.fakebook.SocialMediaApp.databinding.ActivityPostSearchBinding
import com.fakebook.SocialMediaApp.helpers.RankingUtils
import com.fakebook.SocialMediaApp.models.Post
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostSearchActivity : AppCompatActivity()
{

    // view binding
    private lateinit var binding: ActivityPostSearchBinding

    // region view components

    private lateinit var bnvNavbar: BottomNavigationView
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvPosts: RecyclerView
    private lateinit var tvNoPosts: TextView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var tvResultsInfo: TextView

    // endregion

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view binding
        binding = ActivityPostSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // region Initialize view components

        bnvNavbar = binding.bnvNavbar
        etSearch = binding.etSearch
        rvPosts = binding.rvPosts
        tvNoPosts = binding.tvNoPosts
        tilSearch = binding.tilSearch
        tvResultsInfo = binding.tvResultsInfo

        // endregion

        firestore = FirebaseFirestore.getInstance()

        // Highlight the Search item
        bnvNavbar.menu.findItem(R.id.miSearch).isChecked = true

        setUpOnClickListeners()

        rvPosts.visibility = View.GONE
        tvNoPosts.visibility = View.VISIBLE
    }

    private fun setUpOnClickListeners()
    {
        etSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH)
            {
                val query = etSearch.text.toString()

                lifecycleScope.launch {

                    val posts = performSearch(query)
                    setUpRecyclerView(posts, query)

                    // Clear focus from the EditText
                    etSearch.clearFocus()

                    // clear the text in the EditText
                    etSearch.text?.clear()

                    // hide the keyboard
                    etSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
                }

                true
            }
            else
            {
                false
            }
        }

        tilSearch.setEndIconOnClickListener{

            val query = etSearch.text.toString()

            lifecycleScope.launch {
                val posts = performSearch(query)
                setUpRecyclerView(posts, query)

                etSearch.clearFocus()

                etSearch.text?.clear()

                etSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
            }

        }


        // Set up Bottom Navigation View onClickListener
        bnvNavbar.setOnItemSelectedListener {

            when (it.itemId)
            {
                R.id.miHome -> {

                    // navigate to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                R.id.miSearch -> true

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

    private suspend fun performSearch(query: String): List<Post> {

        if (query.isEmpty())
        {
            Snackbar.make(binding.root, "Search query cannot be empty", Snackbar.LENGTH_SHORT).show()

            return emptyList()
        }

        return withContext(Dispatchers.IO)
        {
            try
            {
                val querySnapshot = firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val posts = querySnapshot.documents.mapNotNull { it.toObject(Post::class.java) }

                RankingUtils.rankPostSearch(query, posts)

            }
            catch (e: Exception)
            {
                Log.e("PostSearchActivity", "Error loading posts", e)

                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Failed to load posts", Snackbar.LENGTH_SHORT).show()
                }

                emptyList()
            }
        }
    }



    @SuppressLint("SetTextI18n")
    private fun setUpRecyclerView(posts: List<Post>, query: String)
    {
        if (posts.isNotEmpty())
        {
            rvPosts.adapter = ProfileAdapter(posts)
            rvPosts.layoutManager = GridLayoutManager(this, 3)

            rvPosts.visibility = View.VISIBLE
            tvNoPosts.visibility = View.GONE
            tvResultsInfo.visibility = View.VISIBLE

            tvResultsInfo.text = "Showing results for: $query"
        }
        else
        {
            rvPosts.visibility = View.GONE
            tvNoPosts.visibility = View.VISIBLE
            tvResultsInfo.visibility = View.GONE
        }
    }

}
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
import com.fakebook.SocialMediaApp.adapters.UserAdapter
import com.fakebook.SocialMediaApp.databinding.ActivityUserSearchBinding
import com.fakebook.SocialMediaApp.helpers.RankingUtils
import com.fakebook.SocialMediaApp.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSearchBinding

    private lateinit var firestore: FirebaseFirestore

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvUsers: RecyclerView
    private lateinit var tvNoUsers: TextView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var tvResultsInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        etSearch = binding.etSearch
        rvUsers = binding.rvUsers
        tvNoUsers = binding.tvNoUsers
        tilSearch = binding.tilSearch
        tvResultsInfo = binding.tvResultsInfo

        firestore = FirebaseFirestore.getInstance()

        rvUsers.visibility = View.GONE
        tvNoUsers.visibility = View.VISIBLE

        setUpOnClickListeners()
    }

    private fun setUpOnClickListeners() {
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString()

                lifecycleScope.launch {
                    val users = performSearch(query)
                    setUpRecyclerView(users, query)

                    // Clear focus from the EditText
                    etSearch.clearFocus()

                    // clear the text in the EditText
                    etSearch.text?.clear()

                    // hide the keyboard
                    etSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
                }
                true
            }
            else {
                false
            }
        }

        tilSearch.setEndIconOnClickListener{
            val query = etSearch.text.toString()

            lifecycleScope.launch {
                val users = performSearch(query)
                setUpRecyclerView(users, query)
                etSearch.clearFocus()
                etSearch.text?.clear()
                etSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
            }
        }
    }

    private suspend fun performSearch(query: String): List<User> {
        if (query.isEmpty()) {
            Snackbar.make(binding.root, "Search query cannot be empty", Snackbar.LENGTH_SHORT).show()
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = firestore.collection("users")
                    .get()
                    .await()

                val users = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }

                RankingUtils.rankUserSearch(query, users)

            } catch (e: Exception) {
                Log.e("PostSearchActivity", "Error loading posts", e)
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Failed to load posts", Snackbar.LENGTH_SHORT).show()
                }
                emptyList()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpRecyclerView(users: List<User>, query: String)
    {
        if (users.isNotEmpty())
        {
            rvUsers.adapter = UserAdapter(users) { user -> onUserClicked(user) }
            rvUsers.layoutManager = GridLayoutManager(this, 3)

            rvUsers.visibility = View.VISIBLE
            tvNoUsers.visibility = View.GONE
            tvResultsInfo.visibility = View.VISIBLE

            tvResultsInfo.text = "Showing results for: $query"
        }
        else
        {
            rvUsers.visibility = View.GONE
            tvNoUsers.visibility = View.VISIBLE
            tvResultsInfo.visibility = View.GONE
        }
    }

    private fun onUserClicked(user: User) {
        Intent(this, ProfileActivity::class.java).also {
            it.putExtra("USER_ID", user.userId)
            startActivity(it)
        }
    }
}
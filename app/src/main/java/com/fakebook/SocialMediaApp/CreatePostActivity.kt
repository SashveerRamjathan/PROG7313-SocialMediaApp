package com.fakebook.SocialMediaApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fakebook.SocialMediaApp.databinding.ActivityCreatePostBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreatePostActivity : AppCompatActivity()
{
    // view binding
    private lateinit var binding: ActivityCreatePostBinding

    // view components
    private lateinit var ivPostPic: ImageView
    private lateinit var etCaption: EditText
    private lateinit var btnCreatePost: Button
    private lateinit var btnAddPostPicture: Button
    private lateinit var bnvNavbar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view binding
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize view components
        ivPostPic = binding.ivPostPic
        etCaption = binding.etCaption
        btnCreatePost = binding.btnCreatePost
        btnAddPostPicture = binding.btnAddPostPicture
        bnvNavbar = binding.bnvNavbar

        // highlight the post menu item
        bnvNavbar.menu.findItem(R.id.miPost).isChecked = true

        btnAddPostPicture.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("Image Source")
                .setMessage("Choose image from")
                .setPositiveButtonIcon(ContextCompat.getDrawable(this, R.drawable.ic_camera))
                .setPositiveButton("Camera") { _, _ ->

                    Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show()
                    // launch camera
                }
                .setNegativeButtonIcon(ContextCompat.getDrawable(this, R.drawable.ic_gallery))
                .setNegativeButton("Gallery") { _, _ ->

                    Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show()
                    // launch gallery
                }
                .show()
        }


        btnCreatePost.setOnClickListener {

            // create post
            Toast.makeText(this, "Create Post", Toast.LENGTH_SHORT).show()
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

                R.id.miPost -> true

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
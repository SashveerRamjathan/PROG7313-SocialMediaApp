package com.fakebook.SocialMediaApp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fakebook.SocialMediaApp.databinding.ActivityCreatePostBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayOutputStream
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
 // import io.github.jan.supabase.storage.upload
import io.ktor.http.ContentType
import kotlinx.coroutines.launch

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

    // Image for the selected post picture
    private var image: ByteArray = ByteArray(0)

    // Post ID
    private var postID: String = ""

    // region Supabase Credentials
    private val supabaseUrl = "https://tegyzsstiwjrixqifddn.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRlZ3l6c3N0aXdqcml4cWlmZGRuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQwMDY2ODksImV4cCI6MjA1OTU4MjY4OX0.YvSCHiD2ZlcedWuOBy37CJWR-BXEHXTYKWSEfOTwRBw"
    //endregion

    // Supabase client
    private val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Postgrest)
        install(Storage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

                    // Request camera permission
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    {
                        // Permission already granted—launch camera
                        pickImageFromCamera()
                    }
                    else
                    {
                        // Request the CAMERA permission at runtime.
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                .setNegativeButtonIcon(ContextCompat.getDrawable(this, R.drawable.ic_gallery))
                .setNegativeButton("Gallery") { _, _ ->

                    pickImageFromGallery()
                }
                .show()
        }


        btnCreatePost.setOnClickListener {

            // get caption
            val caption = etCaption.text.toString()

            if (image.isNotEmpty() && caption.isNotEmpty())
            {
                lifecycleScope.launch {

                    val link = uploadImageToStorage()
                    Toast.makeText(this@CreatePostActivity, link, Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Please select an image and enter a caption", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        // region Bottom Navigation View Controls
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
        // endregion
    }

    // region Supabase Methods

    // Upload image to Supabase Storage and return the public URL
    private suspend fun uploadImageToStorage(): String
    {
        try
        {
            // Initialize the storage bucket
            val bucket = supabase.storage.from("banterbox-posts")

            // Generate Post ID
            postID = generatePostID()

            // Upload the image to the specified file path within the bucket
            bucket.upload(postID, image)
            {
                upsert = false // Set to true to overwrite if the file already exists
                contentType = ContentType.Image.JPEG // Set the content type to JPEG
            }

            // Retrieve and return the public URL of the uploaded image
            return bucket.publicUrl(postID)

        } catch (e: Exception)
        {
            // log the error
            Log.e("CreatePostActivity", "Error uploading image to Supabase", e)
            return ""
        }
    }

    private fun generatePostID(): String
    {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')

        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }
    // endregion

    // region Image Picker Methods

    // Function to open the camera and let the user take a photo
    private fun pickImageFromCamera()
    {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageCaptureLauncher.launch(cameraIntent)  // Opens the camera to take an image
    }

    // Function to open the gallery and let the user pick an image
    private fun pickImageFromGallery()
    {
        pickImageLauncher.launch("image/*")  // Opens the gallery to pick an image
    }
    // endregion

    // region Image Activity Launchers

    // Register a launcher for requesting CAMERA permission.
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted)
            {
                // The permission is granted, you can proceed to launch the camera.
                pickImageFromCamera()
            }
            else
            {
                Toast.makeText(
                    this,
                    "Camera permission is required to take a photo.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Activity result launcher for capturing an image from the camera
    private val imageCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK)
            {
                val photo = result.data?.extras?.get("data") as? Bitmap

                photo?.let {
                    ivPostPic.setImageBitmap(it) // Display the selected image

                    // Convert the image bitmap to a ByteArray
                    val byteArray = bitmapToByteArray(it)
                    Log.d("Camera", "Byte array size: ${byteArray.size}")

                    if (byteArray.isNotEmpty())
                    {
                        image = byteArray // Update the image variable
                    }
                    else
                    {
                        Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
                        Log.e("CreateUserProfileActivity", "Image conversion failed")
                    }
                }
            }
        }

    // Activity result launcher for selecting an image from the gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

            // If the user selects an image, update the ImageView
            uri?.let {
                ivPostPic.setImageURI(it)  // Display the selected image

                // Convert the image URI to a Base64 string
                val convertedImage = uriToByteArray(it)

                if (convertedImage?.isNotEmpty() == true)
                {
                    image = convertedImage // Update the image variable
                }
                else
                {
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
                    Log.e("CreateUserProfileActivity", "Image conversion failed")
                }
            }
        }
    // endregion

    // region Image Converters

    // Function to convert image URI to ByteArray
    private fun uriToByteArray(uri: Uri): ByteArray?
    {
        return try
        {
            contentResolver.openInputStream(uri)?.use { inputStream ->

                ByteArrayOutputStream().use { outputStream ->

                    // Copy the entire input stream data into the output stream
                    inputStream.copyTo(outputStream)
                    outputStream.toByteArray()
                }
            }
        }
        catch (e: Exception)
        {
            Log.e("CreateUserProfileActivity", "Error converting image URI to ByteArray", e)
            ByteArray(0) // Return an empty byte array on error
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray
    {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
    // endregion
}
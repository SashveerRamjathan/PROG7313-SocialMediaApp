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
import com.fakebook.SocialMediaApp.databinding.ActivityUpdateAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import java.io.ByteArrayOutputStream

class UpdateAccountActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityUpdateAccountBinding

    // View Components
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnUpdateProfilePicture: Button
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var btnUpdateAccount: Button

    // Firebase Auth
    private lateinit var auth: FirebaseAuth

    // Firebase FireStore
    private lateinit var firestore: FirebaseFirestore

    // Current user
    private var currentUser: FirebaseUser? = null

    // Image for the selected profile picture
    private var image: ByteArray = ByteArray(0)

    // ****move supabase into on create****

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

        // Initialize View Binding
        binding = ActivityUpdateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize View Components
        ivProfilePicture = binding.ivProfilePic
        btnUpdateProfilePicture = binding.btnUpdateProfilePicture
        etFullName = binding.etFullName
        etUsername = binding.etUsername
        etBio = binding.etBio
        btnUpdateAccount = binding.btnUpdate

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase FireStore
        firestore = FirebaseFirestore.getInstance()

        // Set up OnClickListener
        setUpOnClickListener()


    }

    private fun setUpOnClickListener()
    {
        btnUpdateProfilePicture.setOnClickListener {

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

        btnUpdateAccount.setOnClickListener {

            // Toast to simulate update
            Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show()

            // TO DO
            TODO("Update account information")
        }
    }

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
                @Suppress("DEPRECATION")
                val photo = result.data?.extras?.get("data") as? Bitmap

                photo?.let {
                    ivProfilePicture.setImageBitmap(it) // Display the selected image

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
                ivProfilePicture.setImageURI(it)  // Display the selected image

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
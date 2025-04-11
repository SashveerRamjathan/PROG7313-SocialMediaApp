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
import com.fakebook.SocialMediaApp.DataModels.User
import com.fakebook.SocialMediaApp.databinding.ActivityCreateUserProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateUserProfileActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityCreateUserProfileBinding

    // View components
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnAddProfilePicture: Button
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var btnCreateAccount: Button

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Firebase FireStore
    private lateinit var firestore: FirebaseFirestore

    // Image for the selected profile picture
    private var image: ByteArray = ByteArray(0)

    // Current user
    private var currentUser: FirebaseUser? = null

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
        binding = ActivityCreateUserProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Initialize view components
        ivProfilePicture = binding.ivProfilePic
        btnAddProfilePicture = binding.btnAddProfilePicture
        etFullName = binding.etFullName
        etUsername = binding.etUsername
        etBio = binding.etBio
        btnCreateAccount = binding.btnCreate

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase FireStore
        firestore = FirebaseFirestore.getInstance()

        // Get email and password from intent
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        // check email and password
        if (email.isBlank() || password.isBlank()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return  // Prevent further execution
        }

        btnAddProfilePicture.setOnClickListener {
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


        btnCreateAccount.setOnClickListener {

            // Get values from fields
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val bio = etBio.text.toString().trim()

            // Check if all fields are filled
            if (fullName.isNotEmpty() && username.isNotEmpty() && bio.isNotEmpty() && image.isNotEmpty()) {
                // create user in Firebase Authentication
                registerUser(email, password) { user ->
                    if (user != null) {
                        currentUser = user

                        // get the users id
                        val userId = currentUser!!.uid

                        // get the users email
                        val userEmail = currentUser!!.email

                        lifecycleScope.launch {

                            val imageUrl = uploadImageToStorage(userId)

                            // check if link is not empty
                            if (imageUrl.isNotBlank())
                            {
                                // create a user object
                                val newUser = User(
                                    userId = userId,
                                    email = userEmail ?: "",
                                    username = username,
                                    fullName = fullName,
                                    bio = bio,
                                    profilePictureLink = imageUrl
                                )

                                // add user to Firebase FireStore
                                firestore.collection("users").document(newUser.userId).set(newUser)

                                    .addOnSuccessListener {

                                        // display toast
                                        Toast.makeText(
                                            this@CreateUserProfileActivity,
                                            "Profile Created Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // navigate to main activity
                                        val intent = Intent(this@CreateUserProfileActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }

                                    .addOnFailureListener { e ->
                                        // log the error
                                        Log.e(
                                            "CreateUserProfileActivity",
                                            "Error adding user to FireStore",
                                            e
                                        )

                                        // display toast
                                        Toast.makeText(this@CreateUserProfileActivity, "Error Saving Profile", Toast.LENGTH_SHORT)
                                            .show()

                                        // navigate back to userEmailPasswordActivity
                                        val intent = Intent(this@CreateUserProfileActivity, UserEmailPasswordActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }

                    }
                }
            } else {
                // show error message
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun registerUser(email: String, password: String, callback: (FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)

            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    // Registration successful
                    Toast.makeText(this, "User Registration Successful", Toast.LENGTH_SHORT).show()
                    Log.d("CreateUserProfileActivity", "User Registration Successful")

                    // Get the currently signed-in user
                    val user = auth.currentUser

                    callback(user)  // Return user via callback
                } else if (task.exception is FirebaseAuthUserCollisionException) {
                    // Registration failed due to email already in use
                    Toast.makeText(this, "Email already in use", Toast.LENGTH_SHORT).show()
                    Log.e("CreateUserProfileActivity", "Email already in use", task.exception)

                    callback(null)  // Return null if registration fails
                } else {
                    // Registration failed
                    Toast.makeText(
                        this,
                        "Registration Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("CreateUserProfileActivity", "Registration Failed", task.exception)

                    callback(null)  // Return null if registration fails
                }
            }

            .addOnFailureListener { e ->

                // Registration failed
                Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateUserProfileActivity", "Registration Failed", e)

                callback(null)  // Return null if registration fails
            }
    }

    // region Supabase Methods

    // Upload image to Supabase Storage and return the public URL
    private suspend fun uploadImageToStorage(userId: String): String
    {
        try
        {
            // check if post ID is not empty and image is not empty
            if (userId.isNotEmpty() && image.isNotEmpty())
            {
                // Initialize the storage bucket
                val bucket = supabase.storage.from("banterbox-user-profiles")

                // Upload the image to the specified file path within the bucket
                bucket.upload(userId, image)
                {
                    upsert = false // Set to true to overwrite if the file already exists
                    contentType = ContentType.Image.JPEG // Set the content type to JPEG
                }

                // Retrieve and return the public URL of the uploaded image
                return bucket.publicUrl(userId)
            }
            else
            {
                // log the error
                Log.e("CreatePostActivity", "User ID or image is empty")
                return ""
            }
        } catch (e: Exception)
        {
            // log the error
            Log.e("CreatePostActivity", "Error uploading image to Supabase", e)
            return ""
        }
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
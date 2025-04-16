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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fakebook.SocialMediaApp.databinding.ActivityUpdateProfileBinding
import com.fakebook.SocialMediaApp.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class UpdateProfileActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityUpdateProfileBinding

    // View Components
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnUpdateProfilePicture: Button
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var btnUpdateAccount: Button
    private lateinit var btnBack: ImageButton

    // Firebase Auth
    private lateinit var auth: FirebaseAuth

    // Firebase FireStore
    private lateinit var firestore: FirebaseFirestore

    // Current user
    private var currentUser: FirebaseUser? = null

    // Image for the selected profile picture
    private var image: ByteArray = ByteArray(0)

    // current user profile
    private lateinit var currentUserProfile: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize View Components
        ivProfilePicture = binding.ivProfilePic
        btnUpdateProfilePicture = binding.btnUpdateProfilePicture
        etFullName = binding.etFullName
        etUsername = binding.etUsername
        etBio = binding.etBio
        btnUpdateAccount = binding.btnUpdateProfile
        btnBack = binding.btnBack

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase FireStore
        firestore = FirebaseFirestore.getInstance()

        // region Supabase Credentials
        val supabaseUrl = getString(R.string.supabase_url)
        val supabaseKey = getString(R.string.supabase_api_key)
        //endregion

        // Supabase client
        val supabaseClient = createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
            install(Storage)
        }

        // Get the current user
        currentUser = auth.currentUser

        // check if user is logged in
        if (currentUser == null)
        {
            // redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // current user data
        getUserProfile(currentUser!!.uid) { user ->

            if (user != null)
            {
                currentUserProfile = user

                // Update UI with user data
                etFullName.setText(currentUserProfile.fullName)
                etUsername.setText(currentUserProfile.username)
                etBio.setText(currentUserProfile.bio)

                Picasso.get()
                    .load(currentUserProfile.profilePictureLink)
                    .placeholder(R.drawable.ic_default_image)
                    .into(ivProfilePicture)
            }
        }

        // Set up OnClickListener
        setUpOnClickListener(supabaseClient)

    }

    private fun setUpOnClickListener(supabaseClient: SupabaseClient)
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

            // get the values from the fields
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val bio = etBio.text.toString().trim()
            val userId = currentUser!!.uid

            // check if full name is not empty
            if (fullName.isEmpty())
            {
                etFullName.error = "Full name is required"
                return@setOnClickListener
            }

            // check if username is not empty
            if (username.isEmpty())
            {
                etUsername.error = "Username is required"
                return@setOnClickListener
            }

            // check if bio is not empty
            if (bio.isEmpty()) {
                etBio.error = "Bio is required"
                return@setOnClickListener
            }

            lifecycleScope.launch {

                val imageUrl = if (image.isEmpty())
                {
                    uploadImageToStorage(userId, supabaseClient)
                }
                else
                {
                    currentUserProfile.profilePictureLink
                }


                // check if link is not blank
                if (imageUrl.isNotBlank())
                {
                    // update the user profile
                    val updatedProfile = User(
                        userId = userId,
                        email = currentUser!!.email ?: "",
                        username = username,
                        fullName = fullName,
                        bio = bio,
                        profilePictureLink = imageUrl
                    )

                    updateUserProfile(updatedProfile)

                    // redirect to profile activity
                    val intent = Intent(this@UpdateProfileActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this@UpdateProfileActivity, "Error Updating Profile", Toast.LENGTH_SHORT).show()
                }

            }



        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    // region Firebase Methods
    // update user doc in firestore
    private fun updateUserProfile(updatedProfile: User)
    {
        firestore.collection("users").document(updatedProfile.userId).set(updatedProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Updating Profile", Toast.LENGTH_SHORT).show()
                Log.e("UpdateProfileActivity", "Error updating user profile", e)
                return@addOnFailureListener
            }
    }

    // get user profile from firestore
    private fun getUserProfile(userId: String, callback: (User?) -> Unit)
    {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->

                val user = documentSnapshot.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { exception ->

                Log.e("UpdateProfileActivity", "Error getting user profile", exception)
                callback(null)
            }
    }

    // endregion

    // Upload image to Supabase Storage and return the public URL
    private suspend fun uploadImageToStorage(userId: String, supabaseClient: SupabaseClient): String
    {
        try
        {
            // check if post ID is not empty and image is not empty
            if (userId.isNotEmpty() && image.isNotEmpty())
            {
                // Initialize the storage bucket
                val bucket = supabaseClient.storage.from(getString(R.string.supabase_user_profile_bucket_name))

                // Upload the image to the specified file path within the bucket
                bucket.upload(userId, image)
                {
                    upsert = true // Set to true to overwrite if the file already exists
                    contentType = ContentType.Image.JPEG // Set the content type to JPEG
                }

                // Retrieve and return the public URL of the uploaded image
                return bucket.publicUrl(userId)
            }
            else
            {
                // return the current link
                return currentUserProfile.profilePictureLink
            }
        } catch (e: Exception)
        {
            // log the error
            Log.e("UpdateProfileActivity", "Error uploading image to Supabase", e)
            return ""
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
                        Log.e("UpdateProfileActivity", "Image conversion failed")
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
                    Log.e("UpdateProfileActivity", "Image conversion failed")
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
            Log.e("UpdateProfileActivity", "Error converting image URI to ByteArray", e)
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
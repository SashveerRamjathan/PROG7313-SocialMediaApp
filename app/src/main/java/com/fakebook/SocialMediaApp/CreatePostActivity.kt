package com.fakebook.SocialMediaApp

// import io.github.jan.supabase.storage.upload
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
import com.fakebook.SocialMediaApp.databinding.ActivityCreatePostBinding
import com.fakebook.SocialMediaApp.helpers.SupabaseUtils
import com.fakebook.SocialMediaApp.models.Post
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.withContext
import java.util.UUID

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
    private lateinit var btnBack: ImageButton
    private lateinit var cgPostTags: ChipGroup
    private lateinit var etTags: EditText
    private lateinit var tilTagInput: TextInputLayout

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // FireStore
    private lateinit var firestore: FirebaseFirestore

    // Image for the selected post picture
    private var image: ByteArray = ByteArray(0)

    // Post ID
    private var postID: String = ""

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
        btnBack = binding.btnBack
        cgPostTags = binding.cgPostTags
        etTags = binding.etTag
        tilTagInput = binding.tilTagInput

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize FireStore
        firestore = FirebaseFirestore.getInstance()

        // Initialize Supabase Client
        SupabaseUtils.init(this)

        // Set up OnClickListeners
        setUpOnClickListener()
    }

    private fun setUpOnClickListener()
    {
        btnBack.setOnClickListener {
            finish()
        }

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

        tilTagInput.setEndIconOnClickListener {
            val tagText = etTags.text?.toString()?.trim()

            if (!tagText.isNullOrEmpty()) {
                if (cgPostTags.childCount >= 5) {
                    Toast.makeText(this, "Maximum 5 tags allowed", Toast.LENGTH_SHORT).show()
                    return@setEndIconOnClickListener
                }

                val chip = Chip(this).apply {
                    text = tagText
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { cgPostTags.removeView(this) }
                }

                cgPostTags.addView(chip)
                etTags.setText("") // Clear input
            }
        }

        btnCreatePost.setOnClickListener {

            // get caption
            val caption = etCaption.text.toString()

            if (image.isNotEmpty() && caption.isNotBlank())
            {
                // Generate Post ID
                postID = generatePostID()

                // log the post ID
                Log.d("CreatePostActivity", "Creating post with ID: $postID")

                // check if post ID is not empty
                if (postID.isNotBlank())
                {
                    // upload image to storage
                    lifecycleScope.launch {
                        val imageUrl = SupabaseUtils.uploadPostImageToStorage(postID, image)

                        // check if link is not empty
                        if (imageUrl.isNotBlank())
                        {
                            // get current user
                            val user = auth.currentUser

                            // check if user is not null
                            if (user != null)
                            {
                                // get user ID
                                val userId = user.uid

                                val userDoc = withContext(Dispatchers.IO) {
                                    firestore.collection("users").document(userId).get().await()
                                }


                                // check the document exists
                                if (!userDoc.exists())
                                {
                                    Toast.makeText(this@CreatePostActivity, "User not found, Error Creating Post", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                // get the username
                                val username = userDoc.getString("username") ?: ""

                                // get the selected tags
                                val tags = getAllTags()

                                // create a post object
                                val post = Post(
                                    postId = postID,
                                    userId = userId,
                                    imageUrl = imageUrl,
                                    caption = caption,
                                    timestamp = Timestamp.now(),
                                    username = username,
                                    tags = tags
                                )

                                // add post to firestore
                                firestore.collection("posts").document(postID).set(post)

                                    .addOnSuccessListener {

                                        // display toast
                                        Toast.makeText(this@CreatePostActivity, "Post Created Successfully", Toast.LENGTH_SHORT).show()

                                        // navigate to main activity
                                        startActivity(Intent(this@CreatePostActivity, MainActivity::class.java))
                                        finish()
                                    }

                                    .addOnFailureListener { e ->

                                        // log the error
                                        Log.e("CreatePostActivity", "Error adding post to Firestore", e)

                                        // display toast
                                        Toast.makeText(this@CreatePostActivity, "Error Saving Post", Toast.LENGTH_SHORT).show()

                                        // reset post ID
                                        postID = ""
                                    }
                            }
                        }
                    }
                }
                else
                {
                    Toast.makeText(this, "Error generating post ID", Toast.LENGTH_SHORT).show()
                    Log.e("CreatePostActivity", "Post ID is empty")
                    return@setOnClickListener
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

                R.id.miProfile -> {

                    // navigate to profile activity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
        // endregion

    }

    private fun getAllTags(): List<String>
    {
        return (0 until cgPostTags.childCount)
            .mapNotNull { cgPostTags.getChildAt(it) as? Chip }
            .map { it.text.toString() }
    }

    private fun generatePostID(): String = UUID.randomUUID().toString()
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
                @Suppress("DEPRECATION")
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
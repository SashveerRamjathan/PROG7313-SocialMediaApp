package com.fakebook.SocialMediaApp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fakebook.SocialMediaApp.databinding.ActivityCreateUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.util.Base64
import com.fakebook.SocialMediaApp.DataModels.User
import androidx.core.graphics.scale

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

    // Firebase Firestore
    private lateinit var firestore: FirebaseFirestore

    // Image URI for the selected profile picture
    private var base64Image: String = ""

    // Current user
    private var currentUser: FirebaseUser? = null

    // Activity result launcher for selecting an image from the gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // If the user selects an image, update the ImageView
            uri?.let {
                ivProfilePicture.setImageURI(it)  // Display the selected image

                // Convert the image URI to a Base64 string
                val convertedImage = uriToBase64(it)

                if (convertedImage != null)
                {
                    base64Image = convertedImage // Update the base64Image variable
                }
                else
                {
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()

                    Log.e("CreateUserProfileActivity", "Image conversion failed")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityCreateUserProfileBinding.inflate(layoutInflater)

        // Initialize view components
        ivProfilePicture = binding.ivProfilePic
        btnAddProfilePicture = binding.btnAddProfilePicture
        etFullName = binding.etFullName
        etUsername = binding.etUsername
        etBio = binding.etBio
        btnCreateAccount = binding.btnCreate

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get email and password from intent
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        // check email and password
        if (email.isBlank() || password.isBlank())
        {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return  // Prevent further execution
        }

        btnAddProfilePicture.setOnClickListener {
            pickImageFromGallery() // Open the gallery to select an image
        }


        btnCreateAccount.setOnClickListener {

            // Get values from fields
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val bio = etBio.text.toString().trim()

            // Check if all fields are filled
            if (fullName.isNotEmpty() && username.isNotEmpty() && bio.isNotEmpty() && base64Image.isNotBlank())
            {
                // create user in Firebase Authentication
                registerUser(email, password) {user ->
                    if (user != null)
                    {
                        currentUser = user

                        // create a user object
                        val newUser = User(
                            userId = currentUser!!.uid,
                            email = currentUser!!.email ?: "",
                            username = username,
                            fullName = fullName,
                            bio = bio,
                            base64ProfilePicture = base64Image
                        )

                        // add user to Firebase Firestore
                        firestore.collection("users").document(newUser.userId).set(newUser)

                            .addOnSuccessListener {

                                // display toast
                                Toast.makeText(this, "Profile Created Successfully", Toast.LENGTH_SHORT).show()

                                // navigate to main activity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                            .addOnFailureListener { e ->
                                // log the error
                                Log.e("CreateUserProfileActivity", "Error adding user to Firestore", e)

                                // display toast
                                Toast.makeText(this, "Error Saving Profile", Toast.LENGTH_SHORT).show()

                                // navigate back to userEmailPasswordActivity
                                val intent = Intent(this, UserEmailPasswordActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }
            else
            {
                // show error message
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Function to open the gallery and let the user pick an image
    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")  // Opens the gallery to pick an image
    }

    private fun registerUser(email: String, password: String, callback: (FirebaseUser?) -> Unit)
    {
        auth.createUserWithEmailAndPassword(email, password)

            .addOnCompleteListener { task ->

                if (task.isSuccessful)
                {
                    // Registration successful
                    Toast.makeText(this, "User Registration Successful", Toast.LENGTH_SHORT).show()
                    Log.d("CreateUserProfileActivity", "User Registration Successful")

                    // Get the currently signed-in user
                    val user = auth.currentUser

                    callback(user)  // Return user via callback
                }
                else
                {
                    // Registration failed
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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

    // Function to resize the image to a maximum size
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap
    {
        val width = bitmap.width
        val height = bitmap.height
        val scale = maxSize.toFloat() / maxOf(width, height)

        // Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)

        return bitmap.scale((width * scale).toInt(), (height * scale).toInt())
    }

    // Function to convert image URI to Base64 string
    private fun uriToBase64(uri: Uri): String?
    {
        return try
        {
            contentResolver.openInputStream(uri)?.use { inputStream ->

                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                val resizedBitmap = resizeBitmap(originalBitmap, 500)

                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)

            }
        }
        catch (e: Exception)
        {
            Log.e("CreateUserProfileActivity", "Error converting image to Base64", e)
            null
        }
    }

}
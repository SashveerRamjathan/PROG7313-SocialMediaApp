package com.fakebook.SocialMediaApp.helpers

import android.content.Context
import android.util.Log
import com.fakebook.SocialMediaApp.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType

object SupabaseUtils {

    private var supabaseClient: SupabaseClient? = null

    // Initialize the Supabase client once
    fun init(context: Context)
    {
        if (supabaseClient == null)
        {
            val url = context.getString(R.string.supabase_url)
            val key = context.getString(R.string.supabase_api_key)

            supabaseClient = createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            )
            {
                install(Postgrest)
                install(Storage)
            }
        }
    }

    private const val PROFILE_BUCKET = "user-profile"
    private const val POST_BUCKET = "post-images"

    suspend fun uploadProfileImageToStorage(userId: String, image: ByteArray): String
    {
        return uploadImageToBucket(
            bucketName = PROFILE_BUCKET,
            filePath = userId,
            image = image,
            tag = "ProfileUpload"
        )
    }

    suspend fun uploadPostImageToStorage(postId: String, image: ByteArray): String
    {
        return uploadImageToBucket(
            bucketName = POST_BUCKET,
            filePath = postId,
            image = image,
            tag = "PostUpload"
        )
    }

    private suspend fun uploadImageToBucket(bucketName: String, filePath: String, image: ByteArray, tag: String): String
    {
        val client = supabaseClient ?: return ""

        return try
        {
            if (filePath.isNotEmpty() && image.isNotEmpty())
            {
                val bucket = client.storage.from(bucketName)

                bucket.upload(filePath, image)
                {
                    upsert = false
                    contentType = ContentType.Image.JPEG
                }

                bucket.publicUrl(filePath)

            }
            else
            {
                Log.e(tag, "File path or image is empty")
                ""
            }
        }
        catch (e: Exception)
        {
            Log.e(tag, "Error uploading image to Supabase", e)
            ""
        }
    }
}

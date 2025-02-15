package com.example.classescreator.examples

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Paint
import android.media.Image
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.widget.Toast

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import android.media.Image.Plane
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.YuvImage
import android.graphics.Rect
import com.google.firebase.database.ServerValue
import com.kotlinclasses.TPFirebaseDatabase
import com.kotlinclasses.TPFirebaseInstanceDataInterface
import java.io.ByteArrayOutputStream

// Example data class implementing TPFirebaseInstanceDataInterface
data class User(
    override val ID: String,
    val name: String,
    val email: String,
    val age: Int
) : TPFirebaseInstanceDataInterface {

    override var classStructName: String = "User"


//    override fun convertToOriginalInstance(dictionary: Map<String, Any>): Any {
//        return User(
//            ID = dictionary["ID"] as? String ?: "",
//            name = dictionary["name"] as? String ?: "",
//            email = dictionary["email"] as? String ?: "",
//            age = (dictionary["age"] as? Long)?.toInt() ?: 0
//        )
//    }
//
//    override fun convertToMultiOriginalInstances(dictionaries: List<Map<String, Any>>): List<Any> {
//        return dictionaries.map { convertToOriginalInstance(it) }
//    }
//    override fun toMap(): Map<String, Any> {
//        return mapOf(
//            "ID" to ID,
//            "name" to name,
//            "email" to email,
//            "age" to age
//        )
//    }
}



class TPFirebaseDatabaseExample {
    lateinit var database: TPFirebaseDatabase

    init {
        database = TPFirebaseDatabase.getInstance()
    }

    // Example of saving a new value
    fun saveNewValueExample() {
        val nodes = listOf("users", "profiles")

        val data = mapOf(
            "user1" to mapOf(
                "name" to "John Doe",
                "email" to "john@example.com"
            )
        )

        database.saveNewValue(nodes, data) {
            println("Data saved successfully")
        }
    }

    // Example of writing a user instance
    fun writeUserExample() {
        val user = User(
            ID = "user121",
            name = "Jane Doe",
            email = "jane1@example.com",
            age = 25
        )

        database.write(user) { userId ->
            if (userId.isNotEmpty()) {
                println("User written successfully with ID: $userId")
            } else {
                println("Failed to write user")
            }
        }
    }

    // Example of reading a user
    fun readUserExample() {
        val userTemplate = User("user122", "", "", 0)



        database.read(userTemplate) { result ->
            if (result != null && result is User) {
                println("Read user: ${result.name}, Email: ${result.email}")
            } else {
                println("Failed to read user")
            }
        }
    }

    // Example of deleting a user
    fun deleteUserExample() {
        val user = User("user123", "", "", 0)

        database.delete(user) { success, error ->
            if (success) {
                println("User deleted successfully")
            } else {
                println("Failed to delete user: ${error?.message}")
            }
        }
    }

    // Example of saving a local file
    fun saveFileExample() {
        val file = File("example.txt")

        database.saveLocalFile(file) { success ->
            if (success) {
                println("File saved successfully")
            } else {
                println("Failed to save file")
            }
        }
    }

    // Example of deleting a file
    fun deleteFileExample() {
        val filePath = "files/example.txt"

        database.deleteFile(filePath) { success, error ->
            if (success) {
                println("File deleted successfully")
            } else {
                println("Failed to delete file: ${error?.message}")
            }
        }
    }

    // Example of reading an image
    fun readImageExample() {
        val imagePath = "images/example.jpg"

        database.readImage(imagePath) { image ->
            if (image != null) {
                println("Image read successfully")
            } else {
                println("Failed to read image")
            }
        }
    }

    // Example of observing node values
    fun observeNodeValuesExample() {
        val nodes = listOf("users", "profiles")

        database.observeNodeValues(nodes) { values ->
            if (values != null) {
                println("Received updated values: $values")
            } else {
                println("No values available")
            }
        }
    }

    // Example of reading values once
    fun readValuesExample() {
        val nodes = listOf("users", "profiles")

        database.readValues(nodes) { values ->
            if (values != null) {
                println("Read values: $values")
            } else {
                println("No values available")
            }
        }
    }

    // Example of deleting values
    fun deleteValuesExample() {
        val nodes = listOf("users", "profiles")

        database.deleteValues(nodes) { success ->
            if (success) {
                println("Values deleted successfully")
            } else {
                println("Failed to delete values")
            }
        }
    }

    // Example of user authentication
    fun authenticationExample() {
        // Register new user
        database.registerNewUser("user@example.com", "password123")

        // Login
        database.logIn("user@example.com", "password123")

        // Logout
        database.logOff()
    }

    // Example of saving a file from device storage
    fun saveFileFromDeviceExample(context: Context, fileUri: Uri) {
        database.saveFile(
            context = context,
            fileUri = fileUri,
            onProgress = { progress ->
                println("Upload progress: $progress%")
            }
        ) { success ->
            if (success) {
                println("File upload completed successfully")
            } else {
                println("File upload failed")
            }
        }
    }

    // Example of saving an image from device storage
    fun saveImageFromDeviceExample(context: Context, imageUri: Uri) {
        database.saveImage(
            context = context,
            imageUri = imageUri,
            onProgress = { progress ->
                println("Upload progress: $progress%")
            }
        ) { success ->
            if (success) {
                println("Upload completed successfully")
            } else {
                println("Upload failed")
            }
        }
    }

    // Example of saving a file or image from device storage
    fun saveFromDeviceExample(context: Context, uri: Uri, isImage: Boolean) {
        if (isImage) {
            database.saveImage(
                context = context,
                imageUri = uri,
                onProgress = { progress ->
                    println("Image upload progress: $progress%")
                }
            ) { success ->
                if (success) {
                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            database.saveFile(
                context = context,
                fileUri = uri,
                onProgress = { progress ->
                    println("File upload progress: $progress%")
                }
            ) { success ->
                if (success) {
                    Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to upload file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Example of reading a local file URL
    fun readLocalFileExample() {
        val database = TPFirebaseDatabase.getInstance()
        
        // Example file path (use the actual file name that was saved)
        val filePath = "example_document.pdf"
        
        database.readLocalFile(filePath) { fileUrl ->
            if (fileUrl != null) {
                // Successfully got the file URL
                println("File download URL: $fileUrl")
                // You can now use this URL to:
                // 1. Download the file
                // 2. Display it in a WebView
                // 3. Share it with others
            } else {
                println("Failed to get file URL")
            }
        }
    }

    // Example of using all functions together
    fun completeExample(context: Context) {
        // Create and save a user
        writeUserExample()

        // Read the user
        readUserExample()

        // Save some additional data
        saveNewValueExample()

        // Observe changes
        observeNodeValuesExample()

        // Handle files from device - example for both image and file
        val imageUri = Uri.parse("content://your.image.uri")
        val fileUri = Uri.parse("content://your.file.uri")

        // Upload as image
        saveFromDeviceExample(context, imageUri, true)

        // Upload as file
        saveFromDeviceExample(context, fileUri, false)

        // Read a local file URL
        readLocalFileExample()

        // Clean up
//        deleteUserExample()
//        deleteValuesExample()

        // Handle authentication
        authenticationExample()
    }
}

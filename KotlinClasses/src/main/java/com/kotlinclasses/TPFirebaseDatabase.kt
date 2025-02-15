package com.kotlinclasses

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.memberProperties

open class TPFirebaseDatabase {
    var appName: String = ""
        protected set
    var reference: DatabaseReference? = null
        protected set
    var storageReference: StorageReference? = null
        protected set

    constructor(appName: String, databaseReference: DatabaseReference, storageReference: StorageReference?) {
        this.appName = appName
        this.reference = databaseReference
        this.storageReference = storageReference
    }

    companion object {
        @Volatile
        private var instance: TPFirebaseDatabase? = null

        fun getInstance(appName: String = "ExampleApp"): TPFirebaseDatabase {
            return instance ?: synchronized(this) {
                instance ?: initializeDatabase(appName).also { instance = it }
            }
        }

        private fun initializeDatabase(appName: String): TPFirebaseDatabase {
            try {
                val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
                val storageReference: StorageReference? = try {
                    FirebaseStorage.getInstance().reference
                } catch (e: Exception) {
                    Log.e("Firebase", "Error getting storage reference: ${e.message}")
                    null
                }
                return TPFirebaseDatabase(appName, databaseReference, storageReference)
            } catch (e: Exception) {
                Log.e("Firebase", "Error initializing database: ${e.message}")
                throw e
            }
        }
    }

    fun saveNewValue(nodes: List<String>, dictionary: Map<String, Any?>, afterCompletion: () -> Unit) {
        try {
            var currentRef = reference?.child(appName) // Start with app name as top level
            nodes.forEach { node ->
                currentRef = currentRef?.child(node)
            }

            // Update values in Firebase
            currentRef?.updateChildren(dictionary as Map<String, Any>) { error, ref ->
                if (error == null) {
                    afterCompletion()
                } else {
                    // Handle error case
                    println("Error saving value: ${error.message}")
                    afterCompletion()
                }
            }
        } catch (e: Exception) {
            println("Error in saveNewValue: ${e.message}")
            afterCompletion()
        }
    }

    fun write(instance: TPFirebaseInstanceDataInterface, afterComplition: (String) -> Unit) {
        try {
            // Create path using class name and ID under appName
            val nodes = listOf(instance.classStructName, instance.ID)

            // ✅ الحل: استخدام `toMap()` لتحويل الكائن إلى `Map`
            saveNewValue(nodes, instance.toMap()) {
                afterComplition(instance.ID)
            }
        } catch (e: Exception) {
            println("Error in write: ${e.message}")
            afterComplition("")
        }
    }

    // Example of how to use the addObject function:
    // 
    // // Create a Manufacturer object
    // val manufacturer = Manufacturer(
    //     name = "Tech Corp",
    //     contactEmail = "contact@techcorp.com",
    //     contactPhone = "123-456-7890"
    // )
    //
    // // Create a Product object with the manufacturer
    // val product = Product(
    //     ID = "456",
    //     name = "Smartphone",
    //     price = 699.99,
    //     manufacturer = manufacturer
    // )
    //
    // // Add the product to Firebase database
    // TPFirebaseDatabase.getInstance().addObject(product) { id ->
    //     if (id.isNotEmpty()) {
    //         println("Product added successfully. ID: $id")
    //     } else {
    //         println("Failed to add product.")
    //     }
    // }
    fun addObject(instance: TPFirebaseInstanceDataInterface, afterCompletion: (String) -> Unit) {
        try {
            // Create path using class name and ID under appName
            val nodes = listOf(instance.classStructName, instance.ID)

            // Use `toMap()` to convert the object to a Map
            saveNewValue(nodes, instance.toMap()) {
                afterCompletion(instance.ID)
            }
        } catch (e: Exception) {
            println("Error in addObject: ${e.message}")
            afterCompletion("")
        }
    }



    fun read(instance: TPFirebaseInstanceDataInterface, afterComplition: (Any?) -> Unit) {
        try {
            val nodes = listOf(instance.classStructName, instance.ID)
            readValues(nodes) { value ->
                if (value != null) {
                    try {
                        val result = instance.convertToOriginalInstance(value)
                        afterComplition(result)
                    } catch (e: Exception) {
                        println("Error converting data: ${e.message}")
                        afterComplition(null)
                    }
                } else {
                    afterComplition(null)
                }
            }
        } catch (e: Exception) {
            println("Error in read: ${e.message}")
            afterComplition(null)
        }
    }

    fun delete(instance: TPFirebaseInstanceDataInterface, afterComplition: (Boolean, Error?) -> Unit) {
        try {
            val nodes = listOf(instance.classStructName, instance.ID)
            deleteValues(nodes) { success ->
                if (success) {
                    afterComplition(true, null)
                } else {
                    afterComplition(false, Error("Failed to delete"))
                }
            }
        } catch (e: Exception) {
            afterComplition(false, Error(e.message))
        }
    }

    fun readValues(nodes: List<String>, getValue: (Map<String, Any>?) -> Unit) {
        try {
            var currentRef = reference?.child(appName)
            nodes.forEach { node ->
                currentRef = currentRef?.child(node)
            }

            currentRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        getValue(snapshot.value as? Map<String, Any>)
                    } catch (e: Exception) {
                        println("Error converting data: ${e.message}")
                        getValue(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error reading values: ${error.message}")
                    getValue(null)
                }
            })
        } catch (e: Exception) {
            println("Error in readValues: ${e.message}")
            getValue(null)
        }
    }

    fun observeNodeValues(nodes: List<String>, getValue: (Map<String, Any>?) -> Unit) {
        try {
            var currentRef = reference?.child(appName)
            nodes.forEach { node ->
                currentRef = currentRef?.child(node)
            }

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        getValue(snapshot.value as? Map<String, Any>)
                    } catch (e: Exception) {
                        println("Error converting data: ${e.message}")
                        getValue(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error observing values: ${error.message}")
                    getValue(null)
                }
            }

            currentRef?.addValueEventListener(listener)
        } catch (e: Exception) {
            println("Error in observeNodeValues: ${e.message}")
            getValue(null)
        }
    }

    fun deleteValues(nodes: List<String>, afterCompletion: (Boolean) -> Unit) {
        try {
            var currentRef = reference?.child(appName)
            nodes.forEach { node ->
                currentRef = currentRef?.child(node)
            }

            currentRef?.removeValue { error, _ ->
                if (error == null) {
                    afterCompletion(true)
                } else {
                    println("Error deleting values: ${error.message}")
                    afterCompletion(false)
                }
            }
        } catch (e: Exception) {
            println("Error in deleteValues: ${e.message}")
            afterCompletion(false)
        }
    }

    fun readLocalFile(filePath: String, getFile: (Any?) -> Unit) {
        try {
            if (storageReference == null) {
                Log.e("FirebaseStorage", "Storage reference is null, trying to reinitialize")
                storageReference = FirebaseStorage.getInstance().reference
            }

            // تنظيف المسار
            val cleanPath = filePath.trim()
                .replace(Regex("^gs://[^/]+/"), "")
                .replace("\\", "/")
            Log.d("FirebaseStorage", "Attempting to read file: $cleanPath")
            
            // إنشاء مرجع للملف مباشرة من جذر التخزين
            val fileRef = FirebaseStorage.getInstance().reference.child(cleanPath)
            Log.d("FirebaseStorage", "Storage Reference: ${fileRef.path}")
            
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("FirebaseStorage", "Successfully got download URL: $uri")
                getFile(uri.toString())
            }.addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Error getting download URL: ${e.message}")
                Log.e("FirebaseStorage", "Full error: ", e)
                getFile(null)
            }
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error in readLocalFile: ${e.message}")
            Log.e("FirebaseStorage", "Full error: ", e)
            getFile(null)
        }
    }

    fun saveLocalFile(file: Any, afterCompletion: (Boolean) -> Unit) {
        try {
            when (file) {
                is File -> {
                    val fileName = UUID.randomUUID().toString() + "_" + file.name
                    val fileRef = storageReference?.child(appName)?.child("files")?.child(fileName)
                    val uploadTask = fileRef?.putFile(Uri.fromFile(file))

                    uploadTask?.addOnSuccessListener {
                        afterCompletion(true)
                    }?.addOnFailureListener { e ->
                        println("Error uploading file: ${e.message}")
                        afterCompletion(false)
                    }
                }
                else -> {
                    println("Unsupported file type")
                    afterCompletion(false)
                }
            }
        } catch (e: Exception) {
            println("Error in saveLocalFile: ${e.message}")
            afterCompletion(false)
        }
    }

    fun downloadFromFirebase(
        context: Context,
        firebaseDB: TPFirebaseDatabase,
        filePath: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            Log.d("FirebaseDownload", "Starting download for file: $filePath")
            
            firebaseDB.readLocalFile(filePath) { fileUrl ->
                if (fileUrl != null) {
                    Log.d("FirebaseDownload", "Got file URL: $fileUrl")
                    
                    // تحويل الرابط إلى Uri
                    val uri = Uri.parse(fileUrl.toString())
                    val fileName = uri.lastPathSegment ?: "downloaded_file"
                    Log.d("FirebaseDownload", "Parsed filename: $fileName")

                    // إنشاء طلب التحميل
                    val request = DownloadManager.Request(uri).apply {
                        setTitle(fileName)
                        setDescription("جاري تحميل الملف من Firebase...")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        setAllowedOverMetered(true)
                        setAllowedOverRoaming(true)
                    }

                    try {
                        // بدء التحميل
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.enqueue(request)
                        Log.d("FirebaseDownload", "Download started successfully")

                        Toast.makeText(context, "بدأ تحميل الملف", Toast.LENGTH_SHORT).show()
                        onComplete(true, "تم بدء التحميل بنجاح! تحقق من الإشعارات")
                    } catch (e: Exception) {
                        Log.e("FirebaseDownload", "Error starting download: ${e.message}")
                        onComplete(false, "خطأ في بدء التحميل: ${e.message}")
                    }
                } else {
                    Log.e("FirebaseDownload", "File URL is null")
                    onComplete(false, "لم يتم العثور على الملف في Firebase")
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseDownload", "Error in downloadFromFirebase: ${e.message}")
            onComplete(false, "خطأ: ${e.message}")
        }
    }

    fun deleteImage(imagePath: String, completion: (Boolean, Error?) -> Unit) {
        try {
            val imageRef = storageReference?.child(imagePath)
            imageRef?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completion(true, null)
                } else {
                    completion(false, Error(task.exception?.message ?: "Unknown error"))
                }
            } ?: completion(false, Error("Storage reference is null"))
        } catch (e: Exception) {
            completion(false, Error(e.message ?: "Unknown error"))
        }
    }

    fun deleteFile(filePath: String, completion: (Boolean, Error?) -> Unit) {
        try {
            val fileRef = storageReference?.child(filePath)
            fileRef?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completion(true, null)
                } else {
                    completion(false, Error(task.exception?.message ?: "Unknown error"))
                }
            } ?: completion(false, Error("Storage reference is null"))
        } catch (e: Exception) {
            completion(false, Error(e.message ?: "Unknown error"))
        }
    }

    // Helper function to get file name from URI
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        // Try to get the file name from the content resolver
        val cursor = context.contentResolver.query(uri, null, null, null, null)

        return cursor?.use { c ->
            val nameIndex = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && c.moveToFirst()) {
                c.getString(nameIndex)
            } else {
                // If we can't get the name from the content resolver, generate one
                "image_${System.currentTimeMillis()}.jpg"
            }
        } ?: "image_${System.currentTimeMillis()}.jpg"
    }

    fun saveImage(
        context: Context,
        imageUri: Uri,
        onProgress: ((progress: Double) -> Unit)? = null,
        afterCompletion: (Boolean) -> Unit
    ) {
        try {
            // 1. تحويل URI إلى ByteArray
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()

            if (imageBytes == null) {
                Log.e("FirebaseUpload", "Failed to read image data")
                afterCompletion(false)
                return
            }

            // 2. الحصول على اسم الملف
            val fileName = getFileNameFromUri(context, imageUri) ?: "image_${System.currentTimeMillis()}.jpg"

            // 3. إنشاء مرجع للتخزين
            val storageRef = FirebaseStorage.getInstance().reference
                .child("images")
                .child(fileName)

            // 4. رفع البيانات
            val uploadTask = storageRef.putBytes(imageBytes)

            // 5. متابعة عملية الرفع
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // تم الرفع بنجاح
                Log.i("FirebaseUpload", "Image uploaded successfully: ${taskSnapshot.metadata?.path}")

                // الحصول على رابط التحميل
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("FirebaseUpload", "Download URL: $uri")

                    // حفظ الرابط في قاعدة البيانات
                    val database = FirebaseDatabase.getInstance().reference
                    val imageData = mapOf(
                        "url" to uri.toString(),
                        "name" to fileName,
                        "timestamp" to ServerValue.TIMESTAMP
                    )
                    database.child("images").child(fileName.replace(".", "_"))
                        .setValue(imageData)
                        .addOnSuccessListener {
                            afterCompletion(true)
                        }
                        .addOnFailureListener {
                            afterCompletion(false)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("FirebaseUpload", "Upload failed: ${e.message}")
                afterCompletion(false)
            }.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("FirebaseUpload", "Upload progress: $progress%")
                onProgress?.invoke(progress)
            }

        } catch (e: Exception) {
            Log.e("FirebaseUpload", "Error handling image: ${e.message}")
            e.printStackTrace()
            afterCompletion(false)
        }
    }

    fun saveFile(
        context: Context,
        fileUri: Uri,
        onProgress: ((progress: Double) -> Unit)? = null,
        afterCompletion: (Boolean) -> Unit
    ) {
        try {
            // 1. قراءة الملف كـ ByteArray
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()

            if (fileBytes == null) {
                Log.e("FirebaseUpload", "Failed to read file data")
                afterCompletion(false)
                return
            }

            // 2. الحصول على اسم الملف
            val fileName = getFileNameFromUri(context, fileUri) ?: "file_${System.currentTimeMillis()}"

            // 3. إنشاء مرجع للتخزين
            val storageRef = FirebaseStorage.getInstance().reference
                .child("files")
                .child(fileName)

            // 4. رفع البيانات
            val uploadTask = storageRef.putBytes(fileBytes)

            // 5. متابعة عملية الرفع
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // تم الرفع بنجاح
                Log.i("FirebaseUpload", "File uploaded successfully: ${taskSnapshot.metadata?.path}")

                // الحصول على رابط التحميل
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("FirebaseUpload", "Download URL: $uri")

                    // حفظ معلومات الملف في قاعدة البيانات
                    val database = FirebaseDatabase.getInstance().reference
                    val fileData = mapOf(
                        "url" to uri.toString(),
                        "name" to fileName,
                        "size" to fileBytes.size,
                        "timestamp" to ServerValue.TIMESTAMP,
                        "mimeType" to context.contentResolver.getType(fileUri)
                    )
                    database.child("files").child(fileName.replace(".", "_"))
                        .setValue(fileData)
                        .addOnSuccessListener {
                            afterCompletion(true)
                        }
                        .addOnFailureListener {
                            afterCompletion(false)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("FirebaseUpload", "Upload failed: ${e.message}")
                afterCompletion(false)
            }.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("FirebaseUpload", "Upload progress: $progress%")
                onProgress?.invoke(progress)
            }

        } catch (e: Exception) {
            Log.e("FirebaseUpload", "Error handling file: ${e.message}")
            e.printStackTrace()
            afterCompletion(false)
        }
    }


    private fun convertImageToBitmap(image: Image): Bitmap? {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


    fun readImage(imagePath: String, afterCompletion: (Image?) -> Unit) {
        try {
            val imageRef = storageReference?.child(imagePath)
            val ONE_MEGABYTE: Long = 1024 * 1024 * 5 // 5MB max file size

            imageRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener { bytes ->
                try {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    // Convert bitmap to your Image type
                    // This is a placeholder - implement based on your Image type
                    afterCompletion(null)
                } catch (e: Exception) {
                    println("Error converting image: ${e.message}")
                    afterCompletion(null)
                }
            }?.addOnFailureListener { e ->
                println("Error downloading image: ${e.message}")
                afterCompletion(null)
            } ?: afterCompletion(null)
        } catch (e: Exception) {
            println("Error in readImage: ${e.message}")
            afterCompletion(null)
        }
    }

    fun logIn(email: String, password: String) {
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Login successful")
                    } else {
                        println("Login failed: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            println("Error in logIn: ${e.message}")
        }
    }

    fun logOff() {
        try {
            FirebaseAuth.getInstance().signOut()
            println("Logged out successfully")
        } catch (e: Exception) {
            println("Error in logOff: ${e.message}")
        }
    }

    fun registerNewUser(email: String, password: String) {
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("User registered successfully")
                    } else {
                        println("Registration failed: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            println("Error in registerNewUser: ${e.message}")
        }
    }

    // Updates the email address for the currently signed-in user
    fun updateEmail(newEmail: String, onComplete: (Boolean, String?) -> Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                user.updateEmail(newEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(true, null)
                        } else {
                            onComplete(false, task.exception?.message)
                        }
                    }
            } else {
                onComplete(false, "No user is signed in")
            }
        } catch (e: Exception) {
            onComplete(false, e.message)
        }
    }

    // Checks if a user is logged in and returns their UID if they are
    fun checkLoginStatus(): Pair<Boolean, String?> {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            Pair(true, user.uid)
        } else {
            Pair(false, null)
        }
    }
}



interface TPFirebaseInstanceDataInterface : TPFirebaseInstanceFields {

    fun convertToOriginalInstance(dictionary: Map<String, Any>): Any {
        val constructor = this::class.primaryConstructor ?: return this
        val args = constructor.parameters.associateWith { param ->
            val value = dictionary[param.name]

            when {
                value == null -> param.type.defaultValue()
                param.type.classifier == Int::class && value is Number -> value.toInt()
                param.type.classifier == Long::class && value is Number -> value.toLong()
                param.type.classifier == Double::class && value is Number -> value.toDouble()
                param.type.classifier == Boolean::class && value is Boolean -> value
                param.type.classifier == String::class && value is String -> value
                else -> value
            }
        }

        return constructor.callBy(args)
    }

    fun convertToMultiOriginalInstances(dictionaries: List<Map<String, Any>>): List<Any> {
        return dictionaries.map { convertToOriginalInstance(it) }
    }

    fun toMap(): Map<String, Any?> {
        return this::class.memberProperties.associate { prop ->
            prop.name to prop.getter.call(this)
        }
    }
}

fun KType.defaultValue(): Any? {
    return when (this.classifier) {
        String::class -> ""
        Int::class -> 0
        Long::class -> 0L
        Double::class -> 0.0
        Boolean::class -> false
        List::class -> emptyList<Any>()
        Map::class -> emptyMap<Any, Any>()
        else -> null
    }
}

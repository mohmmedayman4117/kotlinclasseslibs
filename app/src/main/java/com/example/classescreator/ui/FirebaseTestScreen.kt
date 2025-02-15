package com.example.classescreator.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kotlinclasses.TPFirebaseDatabase
import com.kotlinclasses.TPFirebaseInstanceDataInterface
import java.util.UUID

// نموذج بيانات للاختبار
data class TestData(
    override val ID: String = "",
    val name: String = "",
    val description: String = "",
    override var classStructName: String = "test_data"
) : TPFirebaseInstanceDataInterface {
    override fun convertToOriginalInstance(dictionary: Map<String, Any>): Any {
        return TestData(
            ID = dictionary["ID"] as? String ?: "",
            name = dictionary["name"] as? String ?: "",
            description = dictionary["description"] as? String ?: ""
        )
    }

    override fun convertToMultiOriginalInstances(dictionaries: List<Map<String, Any>>): List<Any> {
        return dictionaries.map { convertToOriginalInstance(it) }
    }

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "ID" to ID,
            "name" to name,
            "description" to description,
            "classStructName" to classStructName
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseTestScreen() {
    var items by remember { mutableStateOf<List<TestData>>(emptyList()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0.0) }
    var filePath by remember { mutableStateOf("") }
    var downloadStatus by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = remember { TPFirebaseDatabase.getInstance("test_app") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // دالة تحديث القائمة
    fun refreshItems() {
        isRefreshing = true
        database.readValues(listOf("test_data")) { value ->
            if (value != null) {
                val newItems = mutableListOf<TestData>()
                value.forEach { (_, itemData) ->
                    @Suppress("UNCHECKED_CAST")
                    (itemData as? Map<String, Any>)?.let { 
                        TestData().convertToOriginalInstance(it) as? TestData
                    }?.let { 
                        newItems.add(it)
                    }
                }
                items = newItems
            }
            isRefreshing = false
        }
    }

    // تحديث القائمة عند بدء الشاشة
    LaunchedEffect(Unit) {
        refreshItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // تسجيل الدخول
        if (!isLoggedIn) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                database.logIn(email, password)
                                isLoggedIn = true
                            }
                        ) {
                            Text("تسجيل الدخول")
                        }
                        Button(
                            onClick = {
                                database.registerNewUser(email, password)
                            }
                        ) {
                            Text("إنشاء حساب")
                        }
                    }
                }
            }
        } else {
            // زر تحديث القائمة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة العناصر",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = { refreshItems() },
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث"
                        )
                    }
                }
            }

            // إضافة بيانات جديدة
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("الوصف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                val newItem = TestData(
                                    ID = UUID.randomUUID().toString(),
                                    name = name,
                                    description = description
                                )
                                database.write(newItem) { id ->
                                    if (id.isNotEmpty()) {
                                        name = ""
                                        description = ""
                                    }
                                }
                            }
                        ) {
                            Text("إضافة")
                        }
                        Button(
                            onClick = { imagePicker.launch("image/*") }
                        ) {
                            Text("اختيار صورة")
                        }
                    }
                }
            }

            // قسم تحميل الملفات
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "تحميل ملف من Firebase",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = filePath,
                        onValueChange = { filePath = it },
                        label = { Text("مسار الملف") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: images/IMG-20250127-WA0175.jpg") },
                        supportingText = {
                            Text("أدخل المسار النسبي للملف (بدون gs://)")
                        }
                    )
                    
                    if (downloadStatus.isNotEmpty()) {
                        Text(
                            text = downloadStatus,
                            color = if (downloadStatus.contains("خطأ")) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (filePath.isNotEmpty()) {
                                isLoading = true
                                downloadStatus = ""
                                val cleanPath = filePath.trim().replace("\\", "/")
                                try {
                                    downloadFromFirebase(
                                        context = context,
                                        firebaseDB = database,
                                        filePath = cleanPath
                                    ) { success, message ->
                                        isLoading = false
                                        downloadStatus = message
                                        if (success) {
                                            filePath = ""
                                        }
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    downloadStatus = "خطأ: ${e.message}"
                                }
                            } else {
                                downloadStatus = "خطأ: يرجى إدخال مسار الملف"
                            }
                        },
                        enabled = !isLoading && filePath.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLoading) "جاري التحميل..." else "تحميل الملف")
                    }
                }
            }

            // قائمة العناصر
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    var isEditing by remember { mutableStateOf(false) }
                    var editedValue by remember { mutableStateOf(item.description) }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (!isEditing) {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Row {
                                    if (!isEditing) {
                                        IconButton(onClick = { isEditing = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "تعديل"
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            database.delete(item) { success, error ->
                                                if (success) {
                                                    items = items.filter { it.ID != item.ID }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                                    }
                                }
                            }
                            
                            if (isEditing) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editedValue,
                                    onValueChange = { editedValue = it },
                                    label = { Text("الوصف الجديد") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { 
                                            isEditing = false
                                            editedValue = item.description
                                        }
                                    ) {
                                        Text("إلغاء")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val updatedItem = item.copy(description = editedValue)
                                            database.write(updatedItem) { newId ->
                                                if (newId.isNotEmpty()) {
                                                    isEditing = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text("حفظ")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // زر تسجيل الخروج
            Button(
                onClick = {
                    database.logOff()
                    isLoggedIn = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("تسجيل الخروج")
            }
        }
    }

    // مراقبة التغييرات في البيانات
    LaunchedEffect(Unit) {
        database.observeNodeValues(listOf("test_data")) { values ->
            values?.let { map ->
                val testDataList = map.values.mapNotNull { value ->
                    @Suppress("UNCHECKED_CAST")
                    (value as? Map<String, Any>)?.let { TestData().convertToOriginalInstance(it) as? TestData }
                }
                items = testDataList
            }
        }
    }

    // رفع الصورة عند اختيارها
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            database.saveImage(
                context = context,
                imageUri = uri,
                onProgress = { progress ->
                    uploadProgress = progress
                }
            ) { success ->
                if (success) {
                    selectedImageUri = null
                    uploadProgress = 0.0
                }
            }
        }
    }
}

private fun downloadFromFirebase(
    context: Context,
    firebaseDB: TPFirebaseDatabase,
    filePath: String,
    onComplete: (Boolean, String) -> Unit
) {
    try {
        firebaseDB.readLocalFile(filePath) { fileUrl ->
            if (fileUrl != null) {
                // تحويل الرابط إلى Uri
                val uri = Uri.parse(fileUrl.toString())
                val fileName = uri.lastPathSegment ?: "downloaded_file"

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

                    Toast.makeText(context, "بدأ تحميل الملف", Toast.LENGTH_SHORT).show()
                    onComplete(true, "تم بدء التحميل بنجاح! تحقق من الإشعارات")
                } catch (e: Exception) {
                    onComplete(false, "خطأ في بدء التحميل: ${e.message}")
                }
            } else {
                onComplete(false, "لم يتم العثور على الملف في Firebase")
            }
        }
    } catch (e: Exception) {
        onComplete(false, "خطأ: ${e.message}")
    }
}

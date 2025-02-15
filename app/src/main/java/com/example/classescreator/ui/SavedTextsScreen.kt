package com.example.classescreator.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider

import com.kotlinclasses.SavedTextFile
import com.kotlinclasses.TPFileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedTextsScreen() {
    var textFiles by remember { mutableStateOf<List<SavedTextFile>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<SavedTextFile?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }
    var showContentDialog by remember { mutableStateOf<SavedTextFile?>(null) }
    var fileContent by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // تحديث قائمة الملفات
    fun updateFilesList() {
        textFiles = TPFileManager.getAllTextFiles(context)
    }

    LaunchedEffect(Unit) {
        TPFileManager.initialize(context)
        updateFilesList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الملفات النصية") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة ملف")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (textFiles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد ملفات نصية محفوظة",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة ملف نصي")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(textFiles) { file ->
                        TextFileCard(
                            file = file,
                            dateFormat = dateFormat,
                            onDeleteClick = {
                                selectedFile = file
                                showDeleteDialog = true
                            },
                            onOpenClick = {
                                val file = File(file.path)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "text/plain")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "لا يوجد تطبيق لفتح الملفات النصية",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }

            // مربع حوار الحذف
            if (showDeleteDialog && selectedFile != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("حذف الملف") },
                    text = { Text("هل أنت متأكد من حذف الملف ${selectedFile?.name}؟") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedFile?.let { file ->
                                    TPFileManager.deleteThisFile(context, file.name) { error, status ->
                                        if (error == null && status == TPFileManager.Status.EXIST) {
                                            updateFilesList()
                                        }
                                    }
                                }
                                showDeleteDialog = false
                            }
                        ) {
                            Text("حذف")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            // مربع حوار إضافة ملف جديد
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("إضافة ملف نصي جديد") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newFileName,
                                onValueChange = { newFileName = it },
                                label = { Text("اسم الملف") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newFileContent,
                                onValueChange = { newFileContent = it },
                                label = { Text("محتوى الملف") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                maxLines = 10
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newFileName.isNotBlank() && newFileContent.isNotBlank()) {
                                    TPFileManager.saveThisTextToThisFile(
                                        context,
                                        newFileContent,
                                        newFileName
                                    ) { error ->
                                        if (error == null) {
                                            updateFilesList()
                                            newFileName = ""
                                            newFileContent = ""
                                            showAddDialog = false
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("حفظ")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAddDialog = false
                                newFileName = ""
                                newFileContent = ""
                            }
                        ) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            // مربع حوار عرض المحتوى
            showContentDialog?.let { file ->
                AlertDialog(
                    onDismissRequest = { showContentDialog = null },
                    title = { Text(file.name) },
                    text = {
                        Text(
                            text = fileContent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showContentDialog = null }) {
                            Text("إغلاق")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFileCard(
    file: SavedTextFile,
    dateFormat: SimpleDateFormat,
    onDeleteClick: () -> Unit,
    onOpenClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "تم التعديل: ${dateFormat.format(Date(file.lastModified))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "الحجم: ${formatFileSize(file.size)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "حذف")
            }
        }
    }
}

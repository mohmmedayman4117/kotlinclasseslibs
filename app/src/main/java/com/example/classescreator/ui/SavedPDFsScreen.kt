package com.example.classescreator.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import android.content.ActivityNotFoundException
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.kotlinclasses.SavedPdfFile
import com.kotlinclasses.TPFileManager
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPDFsScreen() {
    var pdfFiles by remember { mutableStateOf<List<SavedPdfFile>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<SavedPdfFile?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // تحديث قائمة الملفات
    fun updateFilesList() {
        pdfFiles = TPFileManager.getAllPdfFiles(context)
    }

    // محدد ملفات PDF
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            val fileName = "${System.currentTimeMillis()}.pdf"
            TPFileManager.savePdfFile(context, uri, fileName) { error ->
                isLoading = false
                if (error == null) {
                    updateFilesList()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        TPFileManager.initialize(context)
        updateFilesList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الملفات المحفوظة") },
                actions = {
                    IconButton(onClick = { pdfPicker.launch("application/pdf") }) {
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
            if (pdfFiles.isEmpty()) {
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
                        text = "لا توجد ملفات محفوظة",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { pdfPicker.launch("application/pdf") }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة ملف PDF")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pdfFiles) { file ->
                        PDFFileCard(
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
                                    setDataAndType(uri, "application/pdf")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "لا يوجد تطبيق لفتح ملفات PDF",
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
                                    TPFileManager.deletePdfFile(context, file.name) { error, status ->
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

            // مؤشر التحميل
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFFileCard(
    file: SavedPdfFile,
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

fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    return when {
        kb < 1024 -> String.format("%.1f KB", kb)
        else -> String.format("%.1f MB", kb / 1024)
    }
}

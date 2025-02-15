package com.example.classescreator.ui

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.classescreator.examples.TPFirebaseDatabaseExample
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseUploadScreen(
    context: Context,
    example: TPFirebaseDatabaseExample = TPFirebaseDatabaseExample()
) {
    var uploadProgress by remember { mutableStateOf(0f) }
    var uploadType by remember { mutableStateOf<String?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            selectedUri = newUri
            try {
                example.saveFromDeviceExample(
                    context = context,
                    uri = newUri,
                    isImage = uploadType == "image"
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image Preview (if image is selected)
        if (uploadType == "image" && selectedUri != null) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    uploadType = "image"
                    launcher.launch("image/*")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Upload Image",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Image")
            }

            Button(
                onClick = {
                    uploadType = "file"
                    selectedUri = null
                    launcher.launch("*/*")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Upload File",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload File")
            }
        }

        if (uploadProgress > 0f) {
            LinearProgressIndicator(
                progress = uploadProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Text(
                text = "Upload Progress: ${uploadProgress.toInt()}%",
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Uploading ${if (uploadType == "image") "Image" else "File"}...",
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Show selected file name if it's not an image
        if (uploadType == "file" && selectedUri != null) {
            val fileName = remember(selectedUri) {
                selectedUri?.let { uri ->
                    example.database.getFileNameFromUri(context, uri) ?: "Selected File"
                } ?: "Selected File"
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "File",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fileName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Helper function to get file name from URI
private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null

    try {
        // Try to get the display name from the OpenableColumns
        val cursor: Cursor? = context.contentResolver.query(
            uri, null, null, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }

        // If we couldn't get the display name, try to get the last segment of the URI path
        if (fileName == null) {
            fileName = uri.path?.let { path ->
                path.substring(path.lastIndexOf('/') + 1)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return fileName ?: "Unknown file"
}

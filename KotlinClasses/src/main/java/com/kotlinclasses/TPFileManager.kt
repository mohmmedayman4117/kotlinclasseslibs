package com.kotlinclasses

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.*
import java.net.URL

class TPFileManager {
    companion object {
        private const val PDF_DIRECTORY = "saved_pdfs"
        private const val TEXT_DIRECTORY = "saved_texts"
        
        fun initialize(context: Context) {
            val pdfDir = File(context.filesDir, PDF_DIRECTORY)
            val textDir = File(context.filesDir, TEXT_DIRECTORY)
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }
            if (!textDir.exists()) {
                textDir.mkdirs()
            }
        }

        // دوال الملفات النصية
        fun saveThisTextToThisFile(context: Context, text: String, fileName: String, afterCompletion: (Error?) -> Unit) {
            try {
                val textDir = File(context.filesDir, TEXT_DIRECTORY)
                val file = File(textDir, "$fileName.txt")
                
                FileWriter(file).use { writer ->
                    writer.write(text)
                }
                afterCompletion(null)
            } catch (e: IOException) {
                Log.e("TPFileManager", "Error saving text file", e)
                afterCompletion(Error(e.message))
            }
        }

        fun readFromThisFile(context: Context, fileName: String): String? {
            try {
                val textDir = File(context.filesDir, TEXT_DIRECTORY)
                val file = File(textDir, "$fileName.txt")
                
                if (!file.exists()) return null
                
                return FileReader(file).use { reader ->
                    reader.readText()
                }
            } catch (e: IOException) {
                Log.e("TPFileManager", "Error reading text file", e)
                return null
            }
        }

        fun deleteThisFile(context: Context, fileName: String, afterCompletion: (Error?, Status) -> Unit) {
            try {
                val textDir = File(context.filesDir, TEXT_DIRECTORY)
                val file = File(textDir, "$fileName.txt")
                
                when {
                    !file.exists() -> afterCompletion(null, Status.NOT_EXIST)
                    file.delete() -> afterCompletion(null, Status.EXIST)
                    else -> afterCompletion(Error("Failed to delete file"), Status.FAULT)
                }
            } catch (e: Exception) {
                Log.e("TPFileManager", "Error deleting text file", e)
                afterCompletion(Error(e.message), Status.FAULT)
            }
        }

        fun getAllTextFiles(context: Context): List<SavedTextFile> {
            val textDir = File(context.filesDir, TEXT_DIRECTORY)
            return if (textDir.exists()) {
                textDir.listFiles()
                    ?.filter { it.extension.lowercase() == "txt" }
                    ?.map { 
                        SavedTextFile(
                            name = it.nameWithoutExtension,
                            path = it.absolutePath,
                            size = it.length(),
                            lastModified = it.lastModified()
                        )
                    }
                    ?: emptyList()
            } else {
                emptyList()
            }
        }

        // دوال ملفات PDF
        fun savePdfFile(context: Context, uri: Uri, fileName: String, callback: (Error?) -> Unit) {
            try {
                val pdfDir = File(context.filesDir, PDF_DIRECTORY)
                val destinationFile = File(pdfDir, "$fileName.pdf")
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                callback(null)
            } catch (e: IOException) {
                Log.e("TPFileManager", "Error saving PDF file", e)
                callback(Error(e.message))
            }
        }

        fun getAllPdfFiles(context: Context): List<SavedPdfFile> {
            val pdfDir = File(context.filesDir, PDF_DIRECTORY)
            return if (pdfDir.exists()) {
                pdfDir.listFiles()
                    ?.filter { it.extension.lowercase() == "pdf" }
                    ?.map { 
                        SavedPdfFile(
                            name = it.nameWithoutExtension,
                            path = it.absolutePath,
                            size = it.length(),
                            lastModified = it.lastModified()
                        )
                    }
                    ?: emptyList()
            } else {
                emptyList()
            }
        }

        fun deletePdfFile(context: Context, fileName: String, callback: (Error?, Status) -> Unit) {
            try {
                val pdfDir = File(context.filesDir, PDF_DIRECTORY)
                val file = File(pdfDir, "$fileName.pdf")
                
                when {
                    !file.exists() -> callback(null, Status.NOT_EXIST)
                    file.delete() -> callback(null, Status.EXIST)
                    else -> callback(Error("Failed to delete file"), Status.FAULT)
                }
            } catch (e: Exception) {
                Log.e("TPFileManager", "Error deleting PDF file", e)
                callback(Error(e.message), Status.FAULT)
            }
        }

        fun getPdfFile(context: Context, fileName: String): File? {
            val pdfDir = File(context.filesDir, PDF_DIRECTORY)
            val file = File(pdfDir, "$fileName.pdf")
            return if (file.exists()) file else null
        }
    }

    enum class Status {
        EXIST,
        NOT_EXIST,
        FAULT
    }
}

data class SavedPdfFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)

data class SavedTextFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)
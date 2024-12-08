package com.dicoding.tanicare.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object FileUtil {
    fun from(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val uniqueFileName = "temp_image_${System.currentTimeMillis()}"
        val file = File(context.cacheDir, uniqueFileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        outputStream.close()
        inputStream.close()
        return file
    }
}

fun getMimeTypeFromFile(file: File): String {
    // Mendapatkan ekstensi dari file
    val extension = file.extension.toLowerCase(Locale.getDefault())

    return when (extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "bmp" -> "image/bmp"
        "webp" -> "image/webp"
        else -> "image/*"  // untuk ekstensi file yang tidak dikenal, bisa pakai wildcard
    }
}
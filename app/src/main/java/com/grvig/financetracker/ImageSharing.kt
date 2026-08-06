package com.grvig.financetracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val SHARE_DIR = "shared"

/**
 * Writes the bitmap into the cache directory covered by our FileProvider and
 * returns a content uri other apps are allowed to read.
 */
suspend fun saveShareImage(
    context: Context,
    bitmap: Bitmap,
    fileName: String
): Uri? = withContext(Dispatchers.IO) {

    try {

        val directory = File(context.cacheDir, SHARE_DIR)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Old exports pile up in the cache otherwise.
        directory.listFiles()?.forEach { it.delete() }

        val file = File(directory, fileName)

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

    } catch (e: Exception) {
        null
    }
}

/** Opens the system share sheet so the user picks the destination app. */
fun shareImage(
    context: Context,
    uri: Uri,
    subject: String
) {

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share via")
    )
}

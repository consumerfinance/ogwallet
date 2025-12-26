package dev.consumerfinance.ogwallet.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.OutputStreamWriter

actual class FileSaver : KoinComponent {
    private val context: Context by inject()

    actual suspend fun saveFile(filename: String, content: String, mimeType: String): Boolean {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            // For simplicity, this actual implementation will assume a direct file write
            // which might not be suitable for all Android versions/permissions without SAF.
            // A more robust implementation would integrate with ActivityResultLauncher
            // or request specific permissions.

            // This is a simplified example. In a real app, you'd want to use
            // the Storage Access Framework (SAF) to let the user pick a location.
            // For now, we'll try to save to a file directly, which might only work
            // in app-specific directories without explicit permissions.

            // You might need to adjust this to use MediaStore or SAF for broader compatibility.
            // For a basic internal storage file:
            try {
                // Example of saving to internal storage (app-specific files directory)
                // This doesn't prompt the user for a location.
                context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    OutputStreamWriter(it).use { writer ->
                        writer.write(content)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}

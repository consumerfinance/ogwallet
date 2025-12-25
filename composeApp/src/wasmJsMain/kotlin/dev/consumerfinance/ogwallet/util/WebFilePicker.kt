package dev.consumerfinance.ogwallet.util

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Web file picker for selecting mbox files in browser
 */
object WebFilePicker {
    
    /**
     * Open a file picker dialog and read the selected mbox file
     */
    suspend fun pickAndReadMboxFile(): String? = suspendCoroutine { continuation ->
        // Create a hidden file input element
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = ".mbox,.mbx,text/plain"
        
        input.onchange = { event ->
            val files = input.files
            if (files != null && files.length > 0) {
                val file = files.item(0)

                if (file != null) {
                    // Read the file content
                    val reader = FileReader()
                    reader.onload = { loadEvent ->
                        val content = reader.result as String
                        continuation.resume(content)
                    }
                    reader.onerror = {
                        continuation.resume(null)
                    }
                    reader.readAsText(file)
                } else {
                    continuation.resume(null)
                }
            } else {
                continuation.resume(null)
            }
        }
        
        // Trigger the file picker
        input.click()
    }
}


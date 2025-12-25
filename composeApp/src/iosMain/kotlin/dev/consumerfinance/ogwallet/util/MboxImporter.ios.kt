package dev.consumerfinance.ogwallet.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of MboxImporter using UIDocumentPickerViewController
 */
actual class MboxImporter actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickAndReadMboxFile(): String? = suspendCancellableCoroutine { continuation ->
        try {
            // Get the root view controller
            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

            if (rootViewController == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            // Create document picker for mbox files
            val documentTypes = listOf("public.data", "public.content", "public.text")
            val picker = UIDocumentPickerViewController(
                documentTypes = documentTypes,
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            )

            // Create delegate to handle file selection
            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentAtURL: NSURL
                ) {
                    val fileContent = readFileFromURL(didPickDocumentAtURL)
                    continuation.resume(fileContent)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    continuation.resume(null)
                }
            }

            picker.delegate = delegate
            rootViewController.presentViewController(picker, animated = true, completion = null)

        } catch (e: Exception) {
            println("iOS MboxImporter error: ${e.message}")
            continuation.resume(null)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun readFileFromURL(url: NSURL): String? {
        return try {
            // Start accessing security-scoped resource
            val accessed = url.startAccessingSecurityScopedResource()

            try {
                val data = NSData.dataWithContentsOfURL(url)
                if (data != null) {
                    val string = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                    string?.toString()
                } else {
                    null
                }
            } finally {
                if (accessed) {
                    url.stopAccessingSecurityScopedResource()
                }
            }
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
            null
        }
    }
}


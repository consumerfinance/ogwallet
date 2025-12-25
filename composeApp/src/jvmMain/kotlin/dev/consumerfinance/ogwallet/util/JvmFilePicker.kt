package dev.consumerfinance.ogwallet.util

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop file picker for selecting mbox files
 */
object JvmFilePicker {
    
    /**
     * Open a file picker dialog and return the selected file path
     */
    fun pickMboxFile(): String? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Select mbox file"
        
        // Add file filter for mbox files
        val filter = FileNameExtensionFilter(
            "Mbox Email Files (*.mbox, *.mbx)",
            "mbox", "mbx"
        )
        fileChooser.fileFilter = filter
        fileChooser.isAcceptAllFileFilterUsed = true
        
        val result = fileChooser.showOpenDialog(null)
        
        return if (result == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
    
    /**
     * Read the content of an mbox file
     */
    fun readMboxFile(filePath: String): String {
        return File(filePath).readText()
    }
}


package dev.consumerfinance.ogwallet.auth

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * macOS Touch ID authentication using AppleScript and security command-line tools.
 * This is a pure Kotlin implementation without JNI, using macOS system utilities.
 */
class MacOSTouchIDAuth {
    
    /**
     * Authenticate user with Touch ID using osascript
     * Returns true if authentication successful
     */
    fun authenticate(reason: String): Boolean {
        return try {
            // Use osascript to trigger Touch ID prompt
            val script = """
                use framework "LocalAuthentication"
                use scripting additions
                
                set theContext to current application's LAContext's alloc()'s init()
                set theReason to "$reason"
                
                set {theResult, theError} to theContext's evaluatePolicy:2 localizedReason:theReason reply:(missing value) |error|:(reference)
                
                if theResult is true then
                    return "SUCCESS"
                else
                    return "FAILED"
                end if
            """.trimIndent()
            
            val process = ProcessBuilder(
                "osascript",
                "-l", "AppleScript",
                "-e", script
            ).start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            process.waitFor()
            
            result?.trim() == "SUCCESS"
        } catch (e: Exception) {
            println("Touch ID authentication error: ${e.message}")
            false
        }
    }
    
    /**
     * Get password from macOS Keychain
     */
    fun getKeychainPassword(service: String, account: String): String? {
        return try {
            val process = ProcessBuilder(
                "security",
                "find-generic-password",
                "-s", service,
                "-a", account,
                "-w"  // Output password only
            ).start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val password = reader.readLine()
            val exitCode = process.waitFor()
            
            if (exitCode == 0 && password != null) {
                password.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Keychain read error: ${e.message}")
            null
        }
    }
    
    /**
     * Set password in macOS Keychain
     */
    fun setKeychainPassword(service: String, account: String, password: String): Boolean {
        return try {
            // First, try to delete existing entry
            ProcessBuilder(
                "security",
                "delete-generic-password",
                "-s", service,
                "-a", account
            ).start().waitFor()
            
            // Add new entry
            val process = ProcessBuilder(
                "security",
                "add-generic-password",
                "-s", service,
                "-a", account,
                "-w", password,
                "-U"  // Update if exists
            ).start()
            
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            println("Keychain write error: ${e.message}")
            false
        }
    }
    
    /**
     * Check if Touch ID is available on this Mac
     */
    fun isTouchIDAvailable(): Boolean {
        return try {
            val script = """
                use framework "LocalAuthentication"
                
                set theContext to current application's LAContext's alloc()'s init()
                set {canEvaluate, theError} to theContext's canEvaluatePolicy:2 |error|:(reference)
                
                if canEvaluate is true then
                    return "AVAILABLE"
                else
                    return "NOT_AVAILABLE"
                end if
            """.trimIndent()
            
            val process = ProcessBuilder(
                "osascript",
                "-l", "AppleScript",
                "-e", script
            ).start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            process.waitFor()
            
            result?.trim() == "AVAILABLE"
        } catch (e: Exception) {
            println("Touch ID availability check error: ${e.message}")
            false
        }
    }
}


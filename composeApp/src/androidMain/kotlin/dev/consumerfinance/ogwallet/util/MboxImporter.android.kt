package dev.consumerfinance.ogwallet.util

/**
 * Android implementation of MboxImporter
 * Note: Android uses SMS interception instead of mbox import
 * This is provided for completeness but typically won't be used
 */
actual class MboxImporter actual constructor() {
    actual suspend fun pickAndReadMboxFile(): String? {
        // Android doesn't typically use mbox import since it has SMS access
        // Return null to indicate not supported
        return null
    }
}


package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DriverFactory {
    actual fun createDriver(passphrase: String): SqlDriver {
        // ⚠️ Note: In a real production Web app, you would use the passphrase
        // to derive a key via PBKDF2 and encrypt/decrypt values manually,
        // as SQLCipher doesn't run natively in standard browser SQLite WASM.

        val worker = Worker(js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)"""))

        return WebWorkerDriver(worker).also {
            // Web drivers are usually initialized via a 'init' call
            // Since this factory returns a sync object, ensure your
            // DatabaseManager handles the async loading state.
        }
    }
}
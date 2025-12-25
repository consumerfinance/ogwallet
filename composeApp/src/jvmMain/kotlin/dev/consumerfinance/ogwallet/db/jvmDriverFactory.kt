package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import dev.ogwallet.db.OGVault
import org.sqlite.SQLiteDataSource
import java.io.File

actual class DriverFactory {
    actual fun createDriver(passphrase: String): SqlDriver {
        // 1. Define the database file path (e.g., User Home/OGWallet/vault.db)
        val databasePath = File(System.getProperty("user.home"), ".ogwallet/vault.db")
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }

        // 2. Initialize the JDBC SQLite driver using DataSource
        val dataSource = SQLiteDataSource().apply {
            url = "jdbc:sqlite:${databasePath.absolutePath}"
        }
        val driver: SqlDriver = dataSource.asJdbcDriver()

        // 3. Apply encryption key (SQLCipher for Desktop)
        // Note: Standard SQLite JDBC doesn't support encryption
        // For production, you would need to use SQLCipher JDBC driver
        // driver.execute(null, "PRAGMA key = '$passphrase';", 0)

        // 4. Create the schema if it doesn't exist
        try {
            OGVault.Schema.create(driver)
        } catch (e: Exception) {
            // Schema likely already exists
            println("Schema creation skipped: ${e.message}")
        }

        return driver
    }
}
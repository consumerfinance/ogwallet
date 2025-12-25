// shared/src/iosMain/kotlin/dev/ogwallet/db/IosDriverFactory.kt
package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import dev.ogwallet.db.OGVault

actual class DriverFactory {
    actual fun createDriver(passphrase: String): SqlDriver {
        val schema = OGVault.Schema

        // 1. Create a custom configuration for SQLiter
        val config = DatabaseConfiguration(
            name = "vault.db",
            version = schema.version.toInt(),
            create = { connection ->
                wrapConnection(connection) { schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                wrapConnection(connection) {
                    schema.migrate(it, oldVersion.toLong(), newVersion.toLong())
                }
            },
            // 2. Set the encryption key for SQLCipher
            encryptionConfig = DatabaseConfiguration.Encryption(
                key = passphrase
            )
        )

        // 3. Return the driver using this config
        return NativeSqliteDriver(config)
    }
}
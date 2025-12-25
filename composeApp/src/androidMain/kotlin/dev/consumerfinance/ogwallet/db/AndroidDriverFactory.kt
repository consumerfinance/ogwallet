package dev.consumerfinance.ogwallet.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.ogwallet.db.OGVault
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import net.zetetic.database.sqlcipher.SQLiteDatabase

actual class DriverFactory(private val context: Context) {

    init {
        // Load SQLCipher native libraries
        System.loadLibrary("sqlcipher")
    }

    actual fun createDriver(passphrase: String): SqlDriver {
        val factory = SupportOpenHelperFactory(passphrase.toByteArray())
        return AndroidSqliteDriver(
            schema = OGVault.Schema,
            context = context,
            name = "vault.db",
            factory = factory
        )
    }
}
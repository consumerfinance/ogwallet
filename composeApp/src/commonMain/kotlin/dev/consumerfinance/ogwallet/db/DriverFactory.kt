package dev.consumerfinance.ogwallet.db
import app.cash.sqldelight.db.SqlDriver


expect class DriverFactory {
    fun createDriver(passphrase: String): SqlDriver
}
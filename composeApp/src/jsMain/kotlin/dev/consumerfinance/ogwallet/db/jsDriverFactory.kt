package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlPreparedStatement

actual class DriverFactory {
    actual fun createDriver(passphrase: String): SqlDriver {
        // ⚠️ Note: Stub implementation for web version
        // In a real production Web app, you would use IndexedDB with sql.js
        // For now, returning a minimal stub to allow the app to load
        
        return StubSqlDriver()
    }
}

/**
 * Minimal stub SQL driver for web version
 * This allows the app to compile and run, but database operations won't work
 */
private class StubSqlDriver : SqlDriver {
    override fun close() {}
    
    override fun currentTransaction(): app.cash.sqldelight.Transacter.Transaction? = null
    
    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): app.cash.sqldelight.db.QueryResult<Long> {
        return app.cash.sqldelight.db.QueryResult.Value(0L)
    }
    
    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> app.cash.sqldelight.db.QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): app.cash.sqldelight.db.QueryResult<R> {
        return mapper(StubSqlCursor())
    }
    
    override fun newTransaction(): app.cash.sqldelight.db.QueryResult<app.cash.sqldelight.Transacter.Transaction> {
        return app.cash.sqldelight.db.QueryResult.Value(object : app.cash.sqldelight.Transacter.Transaction() {
            override val enclosingTransaction: app.cash.sqldelight.Transacter.Transaction? = null
            override fun endTransaction(successful: Boolean): app.cash.sqldelight.db.QueryResult<Unit> {
                return app.cash.sqldelight.db.QueryResult.Unit
            }
        })
    }
    
    override fun addListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) {}
    override fun removeListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) {}
    override fun notifyListeners(vararg queryKeys: String) {}
}

private class StubSqlCursor : SqlCursor {
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getLong(index: Int): Long? = null
    override fun getString(index: Int): String? = null
    override fun getBoolean(index: Int): Boolean? = null
    override fun next(): app.cash.sqldelight.db.QueryResult<Boolean> {
        return app.cash.sqldelight.db.QueryResult.Value(false)
    }
}


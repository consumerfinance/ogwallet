package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.consumerfinance.ogwallet.models.TransactionEntry
import dev.consumerfinance.ogwallet.models.CategorySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class TransactionRepository(private val dbManager: DatabaseManager) {

    /**
     * Fetches all transactions and maps them from SQL entities to clean TransactionEntry models.
     * It uses the JOIN logic from the .sq file to handle merchant cleanup.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun getAllTransactions(): Flow<List<TransactionEntry>> {
        val queries = dbManager.queries ?: throw IllegalStateException("Vault Locked")

        return queries.selectAllTransactions()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { row ->
                    TransactionEntry(
                        id = row.id,
                        amount = row.amount,
                        // If we have a clean alias, use it; otherwise, use the raw text
                        merchant = row.alias_name ?: row.merchant_raw,
                        category = row.category,
                        timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                        cardHandle = row.card_handle
                    )
                }
            }
    }

    /**
     * Aggregates spending by category for the Dashboard charts.
     */
    fun getSpendingBreakdown(): Flow<List<CategorySummary>> {
        val queries = dbManager.queries ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return queries.getCategorySummary()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { row ->
                    CategorySummary(
                        category = row.category,
                        totalAmount = row.SUM ?: 0.0,
                        transactionCount = row.COUNT.toInt(),
                        color = getCategoryColor(row.category) // Helper for UI coloring
                    )
                }
            }
    }

    /**
     * Inserts a new transaction (usually called by the SMS Interceptor).
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    suspend fun addTransaction(entry: TransactionEntry) = withContext(Dispatchers.Default) {
        dbManager.queries?.insertTransaction(
            amount = entry.amount,
            currency_code = "USD",
            merchant_raw = entry.merchant,
            category = entry.category,
            card_handle = entry.cardHandle,
            timestamp = entry.timestamp.toEpochMilliseconds()
        )
    }

    private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
        // Logic to return specific colors based on category names
        return androidx.compose.ui.graphics.Color.Gray
    }
}
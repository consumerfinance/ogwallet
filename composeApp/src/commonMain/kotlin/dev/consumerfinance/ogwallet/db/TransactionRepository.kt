package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.consumerfinance.ogwallet.models.TransactionEntry
import dev.consumerfinance.ogwallet.models.CategorySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.* // Import all from kotlinx.datetime
import kotlinx.datetime.DayOfWeek
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds

class TransactionRepository(private val dbManager: DatabaseManager) {

    private fun currentInstant(): Instant = Clock.System.now()

    private fun getStartAndEndOfRange(timeRange: String, now: Instant): Pair<Instant, Instant> {
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return when (timeRange) {
            "week" -> {
                val startOfWeek = localDate.minus(localDate.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
                val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY).atTime(23, 59, 59, 999999999).toInstant(TimeZone.currentSystemDefault())
                startOfWeek.atStartOfDayIn(TimeZone.currentSystemDefault()) to endOfWeek
            }
            "month" -> {
                val startOfMonth = LocalDate(localDate.year, localDate.month, 1)
                val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).atTime(23, 59, 59, 999999999).toInstant(TimeZone.currentSystemDefault())
                startOfMonth.atStartOfDayIn(TimeZone.currentSystemDefault()) to endOfMonth
            }
            "year" -> {
                val startOfYear = LocalDate(localDate.year, Month.JANUARY, 1)
                val endOfYear = startOfYear.plus(1, DateTimeUnit.YEAR).minus(1, DateTimeUnit.DAY).atTime(23, 59, 59, 999999999).toInstant(TimeZone.currentSystemDefault())
                startOfYear.atStartOfDayIn(TimeZone.currentSystemDefault()) to endOfYear
            }
            else -> { // All time
                Instant.fromEpochMilliseconds(0) to now.plus(DatePeriod(years = 100), TimeZone.currentSystemDefault()) // Effectively "all time"
            }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun getTransactionsByTimeRange(timeRange: String): Flow<List<TransactionEntry>> {
        val queries = dbManager.queries ?: throw IllegalStateException("Vault Locked")
        val (start, end) = getStartAndEndOfRange(timeRange, currentInstant())

        return queries.selectTransactionsByTimeRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { row ->
                    TransactionEntry(
                        id = row.id,
                        amount = row.amount,
                        merchant = row.alias_name ?: row.merchant_raw,
                        category = row.category,
                        timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                        cardHandle = row.card_handle
                    )
                }
            }
    }

    /**
     * Aggregates spending by category for the Dashboard charts based on a time range.
     */
    fun getSpendingBreakdownByTimeRange(timeRange: String): Flow<List<CategorySummary>> {
        val queries = dbManager.queries ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        val (start, end) = getStartAndEndOfRange(timeRange, currentInstant())

        return queries.getCategorySummaryByTimeRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { row ->
                    CategorySummary(
                        category = row.category,
                        totalAmount = row.SUM ?: 0.0,
                        transactionCount = row.COUNT.toInt(),
                        color = getCategoryColor(row.category)
                    )
                }
            }
    }

    /**
     * Fetches all transactions and maps them from SQL entities to clean TransactionEntry models.
     * It uses the JOIN logic from the .sq file to handle merchant cleanup.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun getAllTransactions(): Flow<List<TransactionEntry>> {
        return getTransactionsByTimeRange("all")
    }

    /**
     * Aggregates spending by category for the Dashboard charts.
     */
    fun getSpendingBreakdown(): Flow<List<CategorySummary>> {
        return getSpendingBreakdownByTimeRange("all")
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
package dev.consumerfinance.ogwallet.models.parsing

import dev.consumerfinance.ogwallet.models.transactions.TransactionEntry

sealed class ParseResult {
    data class Success(val entry: TransactionEntry) : ParseResult()
    data class Ignored(val reason: String) : ParseResult() // e.g., "Not a transaction message"
    object Failure : ParseResult()
}
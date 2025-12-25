package dev.consumerfinance.ogwallet.models.cards

data class FinancialAccount(
    val id: String,            // Unique ID for the account/card
    val displayName: String,   // "HDFC Regalia"
    val lastFourDigits: String, // "1234"
    val bankName: String,      // "HDFC Bank"
    val accountType: AccountType
)

enum class AccountType {
    CREDIT_CARD, DEBIT_CARD, SAVINGS_ACCOUNT
}
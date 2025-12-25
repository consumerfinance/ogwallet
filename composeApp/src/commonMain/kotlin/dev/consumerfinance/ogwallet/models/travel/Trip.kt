package dev.consumerfinance.ogwallet.models.travel

data class Trip(
    val id: String,
    val destination: String,
    val dates: String,
    val status: String,
    val pointsUsed: Int,
    val savings: Int,
    val emoji: String,
    val bookings: Int
)

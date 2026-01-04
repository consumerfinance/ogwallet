package dev.consumerfinance.ogwallet.models.travel

data class TravelHack(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // e.g., "stopover", "error_fare", "credit_card", "booking_hack"
    val source: String,
    val url: String? = null,
    val tags: List<String> = emptyList(),
    val difficulty: String = "easy", // easy, medium, hard
    val savingsPotential: String? = null,
    val emoji: String = "✈️",
    val upvotes: Int = 0,
    val downvotes: Int = 0
)
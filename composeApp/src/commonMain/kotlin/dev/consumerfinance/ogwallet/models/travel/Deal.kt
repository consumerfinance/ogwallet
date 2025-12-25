package dev.consumerfinance.ogwallet.models.travel

data class TravelDeal(
    val id: String,
    val title: String,
    val destination: String,
    val description: String,
    val savings: Int
)
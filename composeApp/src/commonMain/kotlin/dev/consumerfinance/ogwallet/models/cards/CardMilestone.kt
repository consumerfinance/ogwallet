package dev.consumerfinance.ogwallet.models.cards

import kotlin.time.ExperimentalTime

data class CardMilestone @OptIn(ExperimentalTime::class) constructor(
    val cardName: String,
    val targetAmount: Double,
    val currentSpend: Double,
    val startDate: kotlinx.datetime.Instant,
    val endDate: kotlinx.datetime.Instant,
    val isCompleted: Boolean = false
) {
    val progress: Float get() = (currentSpend / targetAmount).coerceIn(0.0, 1.0).toFloat()
    @OptIn(ExperimentalTime::class)
    val remainingDays: Int get() = ((startDate.epochSeconds-endDate.epochSeconds)/60/60/24).toInt()
}
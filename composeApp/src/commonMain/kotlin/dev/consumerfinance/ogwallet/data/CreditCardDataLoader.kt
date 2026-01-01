package dev.consumerfinance.ogwallet.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.datetime.Instant

/**
 * Platform-specific file reader for loading data files
 */
expect object DataFileReader {
    fun readFile(path: String): String
}

/**
 * Data loader for credit card offers and benefits from git submodule
 */
object CreditCardDataLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Load credit card data from ogwallet-data directory
     */
    fun loadData(): CreditCardDataRepository {
        return try {
            // Try to load from ogwallet-data/data.json
            val dataJson = DataFileReader.readFile("../ogwallet-data/data.json")
            json.decodeFromString(CreditCardDataRepository.serializer(), dataJson)
        } catch (e: Exception) {
            // Fallback to sample data if file not found or parsing fails
            getSampleData()
        }
    }
    
    private fun getSampleData(): CreditCardDataRepository {
        return CreditCardDataRepository(
            offers = listOf(
                CreditCardOfferData(
                    id = "offer-001",
                    title = "5x Points on Dining",
                    description = "Earn 5 points per dollar spent at restaurants and food delivery",
                    cardName = "Chase Sapphire Preferred",
                    bankName = "Chase",
                    category = "dining",
                    expiryDate = "2024-12-31",
                    emoji = "🍽️",
                    gradientColors = listOf("#f97316", "#ef4444"),
                    isActive = true,
                    upvotes = 45,
                    downvotes = 2,
                    source = "crowdsourced"
                ),
                CreditCardOfferData(
                    id = "offer-002",
                    title = "3% Cash Back on Gas",
                    description = "Get 3% cash back on all gas station purchases",
                    cardName = "Citi Custom Cash",
                    bankName = "Citi",
                    category = "gas",
                    expiryDate = "2024-12-20",
                    emoji = "⛽",
                    gradientColors = listOf("#06b6d4", "#0891b2"),
                    isActive = true,
                    maxBenefit = 500.0,
                    upvotes = 38,
                    downvotes = 1,
                    source = "crowdsourced"
                ),
                CreditCardOfferData(
                    id = "offer-003",
                    title = "10% Back at Amazon",
                    description = "Limited time offer for Prime members - 10% back on Amazon purchases",
                    cardName = "Amazon Prime Rewards",
                    bankName = "Chase",
                    category = "shopping",
                    expiryDate = "2024-12-15",
                    emoji = "📦",
                    gradientColors = listOf("#8b5cf6", "#ec4899"),
                    isActive = true,
                    minSpend = 50.0,
                    maxBenefit = 200.0,
                    upvotes = 92,
                    downvotes = 5,
                    source = "crowdsourced"
                )
            ),
            benefits = listOf(),
            redemptionOptions = listOf(
                RedemptionOptionData(
                    id = "redeem-001",
                    name = "Cash Back",
                    rate = "1 point = $0.01",
                    minimumPoints = 2500,
                    emoji = "💵",
                    available = true
                ),
                RedemptionOptionData(
                    id = "redeem-002",
                    name = "Travel Rewards",
                    rate = "1 point = $0.015",
                    minimumPoints = 5000,
                    emoji = "✈️",
                    available = true
                ),
                RedemptionOptionData(
                    id = "redeem-003",
                    name = "Gift Cards",
                    rate = "1 point = $0.012",
                    minimumPoints = 2500,
                    emoji = "🎁",
                    available = true
                ),
                RedemptionOptionData(
                    id = "redeem-004",
                    name = "Statement Credit",
                    rate = "1 point = $0.01",
                    minimumPoints = 2500,
                    emoji = "💳",
                    available = true
                )
            ),
            scrapedPdfs = listOf(),
            lastUpdated = "2024-12-25T00:00:00Z",
            version = "1.0.0"
        )
    }
}

@Serializable
data class CreditCardDataRepository(
    val offers: List<CreditCardOfferData>,
    val benefits: List<CreditCardBenefitData>,
    val redemptionOptions: List<RedemptionOptionData>,
    val scrapedPdfs: List<ScrapedPdfData> = emptyList(),
    val lastUpdated: String,
    val version: String
)

@Serializable
data class ScrapedPdfData(
    val url: String,
    val title: String,
    val content: String,
    val bank: String,
    val scrapedAt: String
)

@Serializable
data class CreditCardOfferData(
    val id: String,
    val title: String,
    val description: String,
    val cardName: String,
    val bankName: String,
    val category: String,
    val expiryDate: String? = null,
    val terms: String? = null,
    val emoji: String = "💳",
    val gradientColors: List<String> = listOf("#3b82f6", "#06b6d4"),
    val isActive: Boolean = true,
    val minSpend: Double? = null,
    val maxBenefit: Double? = null,
    val source: String = "crowdsourced",
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreditCardBenefitData(
    val id: String,
    val cardName: String,
    val bankName: String,
    val benefitType: String,
    val title: String,
    val description: String,
    val value: String,
    val category: String? = null,
    val conditions: String? = null,
    val annualFee: Double? = null,
    val isPermanent: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class RedemptionOptionData(
    val id: String,
    val name: String,
    val rate: String,
    val minimumPoints: Int,
    val emoji: String,
    val available: Boolean = true
)


package dev.consumerfinance.ogwallet.models.travel

import org.maplibre.spatialk.geojson.Position

data class RoutePoint(
    val id: String,
    val position: Position,
    val label: String
) {
    constructor(id: String, lat: Double, lng: Double, label: String) : this(
        id = id,
        position = Position(lat, lng),
        label = label
    )
}

enum class PlanningTab {
    CHECKLIST,
    COSTS,
    REWARDS,
    STOPOVERS,
    ACTIVITIES
}

data class ChecklistItem(
    val id: String,
    val text: String,
    val completed: Boolean = false,
    val category: String
)

data class CostItem(
    val id: String,
    val category: CostCategory,
    val description: String,
    val amount: Double,
    val currency: String = "INR"
)

enum class CostCategory {
    FLIGHT,
    HOTEL,
    RAILWAY,
    OTHER
}

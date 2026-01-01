package dev.consumerfinance.ogwallet.models.travel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Train
import androidx.compose.ui.graphics.vector.ImageVector

enum class CostCategory {
    FLIGHT,
    HOTEL,
    RAILWAY,
    OTHER
}

val CostCategory.icon: ImageVector
    get() = when (this) {
        CostCategory.FLIGHT -> Icons.Default.Flight
        CostCategory.HOTEL -> Icons.Default.Hotel
        CostCategory.RAILWAY -> Icons.Default.Train
        CostCategory.OTHER -> Icons.Default.MoreHoriz
    }

val CostCategory.displayName: String
    get() = when (this) {
        CostCategory.FLIGHT -> "Flights"
        CostCategory.HOTEL -> "Hotels"
        CostCategory.RAILWAY -> "Railway"
        CostCategory.OTHER -> "Other"
    }
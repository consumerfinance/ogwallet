package dev.consumerfinance.ogwallet.models.travel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Train
import androidx.compose.ui.graphics.vector.ImageVector

enum class CostCategory(val categoryIcon: ImageVector, val categoryDisplayName: String) {
    FLIGHT(Icons.Default.Flight, "Flights"),
    HOTEL(Icons.Default.Hotel, "Hotels"),
    RAILWAY(Icons.Default.Train, "Railway"),
    OTHER(Icons.Default.MoreHoriz, "Other")
}
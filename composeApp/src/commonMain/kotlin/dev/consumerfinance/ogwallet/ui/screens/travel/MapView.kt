package dev.consumerfinance.ogwallet.ui.screens.travel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import dev.consumerfinance.ogwallet.models.travel.RoutePoint
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    routes: List<RoutePoint>,
    onMapClick: (Position, DpOffset) -> ClickResult
) {
    // Default camera position for India
    val indiaCenter = Position(20.5937, 78.9629)
    val cameraPositionState = rememberCameraState(
        firstPosition = CameraPosition(
            target = indiaCenter,
            zoom = 5.0
        )
    )

    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        cameraState = cameraPositionState,
        onMapClick = onMapClick,
    ) {
        // Draw markers
        routes.forEach { route ->
            Marker(
                state = MarkerState(position = route.position),
                title = route.label,
                snippet = "${route.position.latitude.format(4)}, ${route.position.longitude.format(4)}"
            )
        }

        // Draw polyline connecting waypoints
        if (routes.size > 1) {
            Polyline(
                points = routes.map { it.position },
                color = Color(0xFF3B82F6),
                width = 10f
            )
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

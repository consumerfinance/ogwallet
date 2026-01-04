package dev.consumerfinance.ogwallet.ui.screens.travel

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.models.travel.RoutePoint
import ogwallet.composeapp.generated.resources.Res
import ogwallet.composeapp.generated.resources.pointer
import org.jetbrains.compose.resources.painterResource
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
        // TODO: Add markers and polylines when available in maplibre-compose
        // Currently placing regular Compose elements inside MaplibreMap causes ClassCastException
        // as the library expects MapNode children, not LayoutNode
    }
}


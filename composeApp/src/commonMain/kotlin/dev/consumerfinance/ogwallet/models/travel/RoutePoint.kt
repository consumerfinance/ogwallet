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

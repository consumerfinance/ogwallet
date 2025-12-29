package dev.consumerfinance.ogwallet.ui.screens.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.models.travel.RoutePoint

@Composable
fun WaypointsOverlay(
    modifier: Modifier = Modifier,
    routes: List<RoutePoint>,
    editingId: String?,
    isMobile: Boolean,
    onEditingIdChange: (String?) -> Unit,
    onRemoveRoute: (String) -> Unit,
    onUpdateRouteLabel: (String, String) -> Unit,
    onClose: (() -> Unit)?
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(if (isMobile) 12.dp else 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Waypoints (${routes.size})",
                        style = if (isMobile) MaterialTheme.typography.titleSmall
                        else MaterialTheme.typography.titleMedium
                    )
                }

                if (onClose != null && isMobile) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Waypoints List
            LazyColumn(
                modifier = Modifier.heightIn(max = if (isMobile) 192.dp else 240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(routes) { index, route ->
                    WaypointItem(
                        index = index,
                        route = route,
                        isEditing = editingId == route.id,
                        isMobile = isMobile,
                        onStartEdit = { onEditingIdChange(route.id) },
                        onEndEdit = { onEditingIdChange(null) },
                        onUpdateLabel = { onUpdateRouteLabel(route.id, it) },
                        onRemove = { onRemoveRoute(route.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WaypointItem(
    index: Int,
    route: RoutePoint,
    isEditing: Boolean,
    isMobile: Boolean,
    onStartEdit: () -> Unit,
    onEndEdit: () -> Unit,
    onUpdateLabel: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Label (editable)
        if (isEditing) {
            var text by remember { mutableStateOf(route.label) }
            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    onUpdateLabel(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (isMobile) MaterialTheme.typography.bodySmall.fontSize
                    else MaterialTheme.typography.bodyMedium.fontSize
                ),
                singleLine = true
            )

            // Auto-blur on focus loss would be handled by LaunchedEffect in real implementation
            LaunchedEffect(Unit) {
                // Focus management would go here
            }
        } else {
            Text(
                text = route.label,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartEdit() }
                    .padding(vertical = 4.dp),
                style = if (isMobile) MaterialTheme.typography.bodySmall
                else MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove waypoint",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

package dev.consumerfinance.ogwallet.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class BadgeType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType = BadgeType.NEUTRAL,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val (backgroundColor, contentColor) = when (type) {
        BadgeType.SUCCESS -> Color(0xFF10b981).copy(alpha = 0.1f) to Color(0xFF10b981)
        BadgeType.WARNING -> Color(0xFFf97316).copy(alpha = 0.1f) to Color(0xFFf97316)
        BadgeType.ERROR -> Color(0xFFef4444).copy(alpha = 0.1f) to Color(0xFFef4444)
        BadgeType.INFO -> Color(0xFF3b82f6).copy(alpha = 0.1f) to Color(0xFF3b82f6)
        BadgeType.NEUTRAL -> Color(0xFF64748b).copy(alpha = 0.1f) to Color(0xFF64748b)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFef4444),
    contentColor: Color = Color.White
) {
    if (count > 0) {
        Surface(
            modifier = modifier.size(20.dp),
            shape = RoundedCornerShape(10.dp),
            color = backgroundColor
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun PercentageBadge(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when {
        percentage < 50 -> Color(0xFF10b981).copy(alpha = 0.1f) to Color(0xFF10b981)
        percentage < 80 -> Color(0xFFf97316).copy(alpha = 0.1f) to Color(0xFFf97316)
        else -> Color(0xFFef4444).copy(alpha = 0.1f) to Color(0xFFef4444)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            "$percentage%",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
fun CategoryBadge(
    category: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

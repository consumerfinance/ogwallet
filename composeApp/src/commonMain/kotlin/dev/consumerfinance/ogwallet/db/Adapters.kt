package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.ColumnAdapter
import dev.consumerfinance.ogwallet.models.AlertPriority
import dev.consumerfinance.ogwallet.models.AlertType
import dev.consumerfinance.ogwallet.models.AuditEventType
import dev.consumerfinance.ogwallet.models.ThemeMode
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}

val booleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue == 1L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}

val auditEventTypeAdapter = object : ColumnAdapter<AuditEventType, String> {
    override fun decode(databaseValue: String): AuditEventType = AuditEventType.valueOf(databaseValue)
    override fun encode(value: AuditEventType): String = value.name
}

val alertTypeAdapter = object : ColumnAdapter<AlertType, String> {
    override fun decode(databaseValue: String): AlertType = AlertType.valueOf(databaseValue)
    override fun encode(value: AlertType): String = value.name
}

val alertPriorityAdapter = object : ColumnAdapter<AlertPriority, String> {
    override fun decode(databaseValue: String): AlertPriority = AlertPriority.valueOf(databaseValue)
    override fun encode(value: AlertPriority): String = value.name
}

val themeModeAdapter = object : ColumnAdapter<ThemeMode, String> {
    override fun decode(databaseValue: String): ThemeMode = ThemeMode.valueOf(databaseValue)
    override fun encode(value: ThemeMode): String = value.name
}
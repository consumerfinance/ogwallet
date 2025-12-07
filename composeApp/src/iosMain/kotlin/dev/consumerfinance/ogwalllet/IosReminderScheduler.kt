package dev.consumerfinance.ogwallet

import kotlinx.datetime.*
import platform.Foundation.*
import platform.UserNotifications.*

actual class ReminderScheduler {

    actual fun schedule(id: String, title: String, body: String, dateTime: LocalDateTime) {

        val triggerSeconds =
            dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds() / 1000.0

        val content = UNMutableNotificationContent().apply {
            this.title = title
            this.body = body
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            triggerSeconds - NSDate().timeIntervalSince1970,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            request
        ) { println("iOS notification error: $0") }
    }
}

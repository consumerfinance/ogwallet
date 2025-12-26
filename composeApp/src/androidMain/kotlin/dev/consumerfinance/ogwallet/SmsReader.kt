package dev.consumerfinance.ogwallet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.compose.runtime.Composable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class SmsReader : KoinComponent {
    private val context: Context by inject()

    @Composable
    actual fun registerSmsReceiver(onSmsReceived: (String) -> Unit) {
        // Implementation for registering a broadcast receiver can be added here
    }

    actual fun readSmsMessages(): List<String> {
        val messages = mutableListOf<String>()
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY),
            null,
            null,
            "${Telephony.Sms.DATE} DESC LIMIT 1000"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                do {
                    messages.add(it.getString(bodyIndex))
                } while (it.moveToNext())
            }
        }
        return messages
    }
}

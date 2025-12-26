package dev.consumerfinance.ogwallet.util

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dev.consumerfinance.ogwallet.OGWalletApplication
import android.content.Context // Added import

actual fun openAppNotificationSettings() {
    val context: Context = OGWalletApplication.appContext
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
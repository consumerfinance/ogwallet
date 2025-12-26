package dev.consumerfinance.ogwallet.util

import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

actual fun openAppNotificationSettings() {
    val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
    if (settingsUrl != null) {
        UIApplication.sharedApplication.openURL(settingsUrl)
    }
}
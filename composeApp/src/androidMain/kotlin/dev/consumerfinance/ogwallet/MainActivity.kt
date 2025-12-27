package dev.consumerfinance.ogwallet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import dev.consumerfinance.ogwallet.auth.BiometricAuth
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.notifications.BillReminderScheduler
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class MainActivity : FragmentActivity() {

    private val dbManager by inject<DatabaseManager>()

    private val autoLockHandler = Handler(Looper.getMainLooper())
    private val autoLockRunnable = Runnable {
        if (dbManager.isUnlocked.value) {
            dbManager.lock()
        }
    }

    private fun resetAutoLockTimer() {
        autoLockHandler.removeCallbacks(autoLockRunnable)
        if (dbManager.isUnlocked.value) {
            val timeout = runBlocking { dbManager.getAutoLockTimeout().first() }
            autoLockHandler.postDelayed(autoLockRunnable, timeout * 1000)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readSmsGranted = permissions[Manifest.permission.READ_SMS] ?: false
        val receiveSmsGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true
        }

        if (readSmsGranted && receiveSmsGranted) {
            android.util.Log.d("MainActivity", "SMS permissions granted")
        } else {
            android.util.Log.w("MainActivity", "SMS permissions denied")
        }

        if (notificationGranted) {
            android.util.Log.d("MainActivity", "Notification permission granted")
            // Schedule bill reminders
            BillReminderScheduler.scheduleDailyBillCheck(this)
        } else {
            android.util.Log.w("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Load activity-specific module for BiometricAuth
        val activityModule = module {
            single { BiometricAuth(this@MainActivity) }
        }
        loadKoinModules(activityModule)

        // Lock vault when app is backgrounded or timeout exceeded
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Check if auto-lock timeout has been exceeded
                    val timeout = runBlocking { dbManager.getAutoLockTimeout().first() }
                    if (dbManager.unlockTime > 0 &&
                        System.currentTimeMillis() - dbManager.unlockTime > timeout * 1000) {
                        dbManager.lock()
                    } else if (dbManager.isUnlocked.value) {
                        resetAutoLockTimer()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    autoLockHandler.removeCallbacks(autoLockRunnable)
                    dbManager.lock()
                }
                else -> {
                    // Ignore other events
                }
            }
        })

        // Request permissions if not granted
        requestPermissions()

        setContent {
            App()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetAutoLockTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        autoLockHandler.removeCallbacks(autoLockRunnable)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsToRequest = mutableListOf<String>()

            // Check SMS permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_SMS)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
            }

            // Check notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                // All permissions already granted, schedule reminders
                BillReminderScheduler.scheduleDailyBillCheck(this)
            }
        }
    }
}
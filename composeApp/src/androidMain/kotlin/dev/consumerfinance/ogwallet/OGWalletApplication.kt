package dev.consumerfinance.ogwallet

import android.app.Application
import dev.consumerfinance.ogwallet.db.DriverFactory
import dev.consumerfinance.ogwallet.di.commonModule
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class OGWalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SQLCipher native libraries
        System.loadLibrary("sqlcipher")

        // Only start Koin if it hasn't been started yet
        if (GlobalContext.getOrNull() == null) {
            val platformModule = module {
                single { DriverFactory(androidContext()) }
            }

            startKoin {
                androidContext(this@OGWalletApplication)
                modules(commonModule, platformModule)
            }
        }
    }
}


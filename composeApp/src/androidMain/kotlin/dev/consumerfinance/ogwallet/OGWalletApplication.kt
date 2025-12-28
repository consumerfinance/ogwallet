package dev.consumerfinance.ogwallet

import android.app.Application
import android.content.Context // Added import
import dev.consumerfinance.ogwallet.db.DriverFactory
import dev.consumerfinance.ogwallet.di.commonModule
import dev.consumerfinance.ogwallet.util.AppContext
import dev.consumerfinance.ogwallet.util.EncryptionUtil
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class OGWalletApplication : Application() {

    companion object {
        lateinit var appContext: Context // Renamed from applicationContext
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext // Corrected initialization

        // Initialize SQLCipher native libraries
        System.loadLibrary("sqlcipher")

        // Only start Koin if it hasn't been started yet
        if (GlobalContext.getOrNull() == null) {
            val platformModule = module {
                single { DriverFactory(androidContext()) }
                single { SmsReader(androidContext()) }
                single<EncryptionUtil> { EncryptionUtil(AppContext(androidContext())) }
            }

            startKoin {
                androidContext(this@OGWalletApplication)
                modules(commonModule, platformModule)
            }
        }
    }
}


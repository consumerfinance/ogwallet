package dev.consumerfinance.ogwallet.di

import dev.consumerfinance.ogwallet.auth.BiometricAuth
import dev.consumerfinance.ogwallet.db.DriverFactory
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    // Only start Koin if it hasn't been started yet
    if (GlobalContext.getOrNull() == null) {
        val platformModule = module {
            single { DriverFactory() }
            single { BiometricAuth() }
        }

        startKoin {
            modules(commonModule, platformModule)
        }
    }
}


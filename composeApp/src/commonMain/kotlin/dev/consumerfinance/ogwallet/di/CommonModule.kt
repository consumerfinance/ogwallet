package dev.consumerfinance.ogwallet.di

import dev.consumerfinance.ogwallet.db.BillRepository
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.db.TransactionRepository
import org.koin.dsl.module

val commonModule = module {
    single { DatabaseManager(get()) }
    single { TransactionRepository(get()) }
    single { BillRepository(get()) }
}
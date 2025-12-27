package dev.consumerfinance.ogwallet.di

import dev.consumerfinance.ogwallet.db.BillRepository
import dev.consumerfinance.ogwallet.db.CreditCardRepository
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.db.TransactionRepository
import org.koin.dsl.module

import dev.consumerfinance.ogwallet.services.DataExportService
import dev.consumerfinance.ogwallet.util.SmsParser

val commonModule = module {
    single { DatabaseManager(get()) }
    single { TransactionRepository(get()) }
    single { BillRepository(get()) }
    single { CreditCardRepository(get()) }
    single { DataExportService(get(), get()) } // Inject TransactionRepository and BillRepository
    single { SmsParser(get()) }
}
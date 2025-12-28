package dev.consumerfinance.ogwallet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.util.SmsParser
import dev.consumerfinance.ogwallet.models.TransactionEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsInterceptor : BroadcastReceiver(), KoinComponent {
    private val repository: TransactionRepository by inject()
    private val smsParser: SmsParser by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.displayMessageBody
                Log.d("SmsInterceptor", "Received SMS: $body")

                val match = smsParser.parse(body)

                if (match != null) {
                    Log.d("SmsInterceptor", "Parsed transaction: ${match.amount} at ${match.merchantRaw}")
                    // This only works if the user has already unlocked the vault!
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.addTransaction(
                                TransactionEntry(
                                    id = 0,
                                    amount = match.amount,
                                    merchant = match.merchantRaw,
                                    category = match.category,
                                    cardHandle = match.accountHandle,
                                    timestamp = Clock.System.now()
                                )
                            )
                            Log.d("SmsInterceptor", "Transaction saved successfully")
                        } catch (e: Exception) {
                            Log.e("SmsInterceptor", "Failed to save transaction: ${e.message}", e)
                        }
                    }
                } else {
                    Log.d("SmsInterceptor", "SMS did not match transaction pattern")
                }
            }
        }
    }
}
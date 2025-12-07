package dev.consumerfinance.ogwallet

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log

actual fun scanSmsForBillingInfo(context: Any): String {
    if (context is Context) {
        val smsUri: Uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS)
        val selection = "${Telephony.Sms.BODY} LIKE ?"
        val selectionArgs = arrayOf("%bill%", "%payment%", "%credit card%", "%bank%")

        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val body = it.getString(it.getColumnIndex(Telephony.Sms.BODY))
                    val address = it.getString(it.getColumnIndex(Telephony.Sms.ADDRESS))

                    if (containsBillingInfo(body)) {
                        return "Found potential bill from $address: $body"
                    }
                } while (it.moveToNext())
            }
        }
    }
    return "No bills found."
}

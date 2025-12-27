package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for SmsParser - validates regex patterns for extracting
 * transaction information from Indian bank SMS messages.
 */
class SmsParserTest {

    private val smsParser = SmsParser(null) // TransactionRepository not needed for parsing

    // ========== HDFC Bank SMS Tests ==========

    @Test
    fun testHDFCBank_standardFormat() {
        val sms = "Rs.399.00 spent on HDFC Bank Card XX1234 at AMAZON PRIME on 21-Dec-24. Avl Bal: Rs.10,832.00"
        val result = smsParser.parse(sms)
        
        assertNotNull(result, "Should parse HDFC SMS")
        assertEquals(399.0, result.amount)
        assertEquals("1234", result.accountHandle)
        assertEquals("AMAZON PRIME", result.merchantRaw)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testHDFCBank_withCommas() {
        val sms = "Rs.1,500.50 spent on HDFC Bank Card XX5678 at FLIPKART on 22-Dec-24"
        val result = smsParser.parse(sms)
        
        assertNotNull(result, "Should parse HDFC SMS with comma in amount")
        assertEquals(1500.5, result.amount)
        assertEquals("5678", result.accountHandle)
    }

    // ========== ICICI Bank SMS Tests ==========
    
    @Test
    fun testICICIBank_debitFormat() {
        val sms = "INR 599.00 debited from Card XX5678 at NETFLIX on 22-Dec-24. Available limit: INR 44,803.00"
        val result = smsParser.parse(sms)
        
        assertNotNull(result, "Should parse ICICI debit SMS")
        assertEquals(599.0, result.amount)
        assertEquals("5678", result.accountHandle)
        assertEquals("NETFLIX", result.merchantRaw)
    }

    @Test
    fun testICICIBank_alternateFormat() {
        val sms = "Debited Rs.2500.00 from card XX9876 at FLIPKART"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse ICICI alternate format")
        assertEquals(2500.0, result.amount)
        assertEquals("9876", result.accountHandle)
        assertEquals("FLIPKART", result.merchantRaw)
    }

    // ========== SBI Card SMS Tests ==========

    @Test
    fun testSBICard_standardFormat() {
        val sms = "Rs.2,100.00 spent on SBI Card XX9012 at TATA POWER on 23-Dec-24. Available credit: Rs.44,301.00"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse SBI Card SMS")
        assertEquals(2100.0, result.amount)
        assertEquals("9012", result.accountHandle)
        assertEquals("TATA POWER", result.merchantRaw)
    }

    // ========== Axis Bank SMS Tests ==========

    @Test
    fun testAxisBank_debitFormat() {
        val sms = "Dear Customer, Rs.1500.00 debited from your Axis Bank Card ending 3456 at SWIGGY on 24-Dec-24"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Axis Bank SMS")
        assertEquals(1500.0, result.amount)
        assertEquals("3456", result.accountHandle)
        assertEquals("SWIGGY", result.merchantRaw)
    }

    // ========== Kotak Bank SMS Tests ==========

    @Test
    fun testKotakBank_transactionFormat() {
        val sms = "Transaction of Rs.750 on Kotak Card XXXX7890 at ZOMATO approved. Avl limit: Rs.25,000"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Kotak Bank SMS")
        assertEquals(750.0, result.amount)
        assertEquals("7890", result.accountHandle)
        assertEquals("ZOMATO", result.merchantRaw)
    }

    // ========== Citi Bank SMS Tests ==========

    @Test
    fun testCitiBank_accountFormat() {
        val sms = "Your Citi Card A/c XX2468 has been debited with Rs.2500 at FLIPKART on 25-Dec-24"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Citi Bank SMS")
        assertEquals(2500.0, result.amount)
        assertEquals("2468", result.accountHandle)
        assertEquals("FLIPKART", result.merchantRaw)
    }

    // ========== American Express SMS Tests ==========

    @Test
    fun testAmex_endingInFormat() {
        val sms = "American Express Card ending in 1357 charged Rs.5000 at APPLE STORE"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Amex SMS")
        assertEquals(5000.0, result.amount)
        assertEquals("1357", result.accountHandle)
    }

    // ========== Various Amount Formats ==========
    
    @Test
    fun testAmountWithoutDecimals() {
        val sms = "Rs.500 spent on Card XX1111 at AMAZON"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse amount without decimals")
        assertEquals(500.0, result.amount)
    }

    @Test
    fun testAmountWithCommasAndDecimals() {
        val sms = "Rs.12,345.67 spent on Card XX2222 at FLIPKART"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse amount with commas and decimals")
        assertEquals(12345.67, result.amount)
    }

    @Test
    fun testLargeAmount() {
        val sms = "Rs.99,999.99 spent on Card XX3333 at JEWELLERY STORE"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse large amount")
        assertEquals(99999.99, result.amount)
    }

    // ========== Merchant Name Variations ==========
    
    @Test
    fun testMerchantWithNumbers() {
        val sms = "Rs.100 spent on Card XX4444 at 7ELEVEN"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse merchant with numbers")
        assertEquals("7ELEVEN", result.merchantRaw)
    }

    @Test
    fun testMerchantWithAmpersand() {
        val sms = "Rs.200 spent on Card XX5555 at MARKS & SPENCER"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse merchant with ampersand")
        assertEquals("MARKS & SPENCER", result.merchantRaw)
    }

    @Test
    fun testMerchantWithDot() {
        val sms = "Rs.300 spent on Card XX6666 at AMAZON.IN"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse merchant with dot")
        assertEquals("AMAZON.IN", result.merchantRaw)
    }

    // ========== Edge Cases ==========
    
    @Test
    fun testNoCardNumber() {
        val sms = "Rs.500 spent at AMAZON on 21-Dec-24"
        val result = SmsParser.parse(sms)
        
        assertNotNull(result, "Should parse even without card number")
        assertEquals("Unknown", result.accountHandle)
    }

    @Test
    fun testInvalidSMS() {
        val sms = "This is not a transaction SMS"
        val result = SmsParser.parse(sms)
        
        assertNull(result, "Should return null for non-transaction SMS")
    }

    @Test
    fun testEmptySMS() {
        val sms = ""
        val result = SmsParser.parse(sms)
        
        assertNull(result, "Should return null for empty SMS")
    }
}


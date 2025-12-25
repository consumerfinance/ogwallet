package dev.consumerfinance.ogwallet.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for Indian credit card number regex patterns.
 * Tests various formats used by Indian banks in SMS notifications.
 */
class IndianCreditCardRegexTest {

    // Regex patterns for Indian credit card formats
    companion object {
        // Pattern 1: "Card XX1234" or "CARD XX1234"
        val cardXXPattern = Regex("(?i)card\\s+xx(\\d{4})")

        // Pattern 2: "Card ending 1234" or "card ending in 1234"
        val cardEndingPattern = Regex("(?i)card\\s+ending(?:\\s+in)?\\s+(\\d{4})")

        // Pattern 3: "Card ****1234" or "Card XXXX1234"
        val cardMaskedPattern = Regex("(?i)card\\s+[*x]{4}(\\d{4})")

        // Pattern 4: "A/c XX1234" or "Acct XX1234"
        val accountPattern = Regex("(?i)(?:a/c|acct|account)\\s+xx(\\d{4})")

        // Pattern 5: Full card number with spaces (4-4-4-4 format)
        val fullCardSpacesPattern = Regex("(\\d{4})\\s+(\\d{4})\\s+(\\d{4})\\s+(\\d{4})")

        // Pattern 6: Full card number without spaces (16 digits)
        val fullCardNoSpacesPattern = Regex("\\b(\\d{16})\\b")

        // Pattern 7: Card number with dashes (4-4-4-4 format)
        val fullCardDashesPattern = Regex("(\\d{4})-(\\d{4})-(\\d{4})-(\\d{4})")

        // Combined pattern that matches all common formats
        val combinedCardPattern = Regex(
            "(?i)(?:card|a/c|acct|account)\\s*(?:ending|xx|no|num|\\*{4}|x{4})?\\s*(?:in)?\\s*(\\d{4})"
        )
    }

    // ========== Pattern 1: "Card XX1234" Tests ==========

    @Test
    fun testCardXXPattern_uppercase() {
        val sms = "Rs.500 spent on Card XX1234 at Amazon"
        val match = cardXXPattern.find(sms)
        assertNotNull(match, "Should match 'Card XX1234'")
        assertEquals("1234", match.groupValues[1], "Should extract last 4 digits")
    }

    @Test
    fun testCardXXPattern_lowercase() {
        val sms = "Rs.500 spent on card xx5678 at Flipkart"
        val match = cardXXPattern.find(sms)
        assertNotNull(match, "Should match 'card xx5678'")
        assertEquals("5678", match.groupValues[1])
    }

    @Test
    fun testCardXXPattern_mixedCase() {
        val sms = "Transaction on CaRd Xx9012 approved"
        val match = cardXXPattern.find(sms)
        assertNotNull(match, "Should match mixed case 'CaRd Xx9012'")
        assertEquals("9012", match.groupValues[1])
    }

    // ========== Pattern 2: "Card ending 1234" Tests ==========

    @Test
    fun testCardEndingPattern_basic() {
        val sms = "Payment from card ending 3456 successful"
        val match = cardEndingPattern.find(sms)
        assertNotNull(match, "Should match 'card ending 3456'")
        assertEquals("3456", match.groupValues[1])
    }

    @Test
    fun testCardEndingPattern_withIn() {
        val sms = "Debit from card ending in 7890 at Swiggy"
        val match = cardEndingPattern.find(sms)
        assertNotNull(match, "Should match 'card ending in 7890'")
        assertEquals("7890", match.groupValues[1])
    }

    @Test
    fun testCardEndingPattern_uppercase() {
        val sms = "CARD ENDING 2468 used for purchase"
        val match = cardEndingPattern.find(sms)
        assertNotNull(match, "Should match uppercase 'CARD ENDING 2468'")
        assertEquals("2468", match.groupValues[1])
    }

    // ========== Pattern 3: "Card ****1234" Tests ==========

    @Test
    fun testCardMaskedPattern_asterisks() {
        val sms = "Transaction on Card ****1357 approved"
        val match = cardMaskedPattern.find(sms)
        assertNotNull(match, "Should match 'Card ****1357'")
        assertEquals("1357", match.groupValues[1])
    }

    @Test
    fun testCardMaskedPattern_X() {
        val sms = "Payment via Card XXXX2468 successful"
        val match = cardMaskedPattern.find(sms)
        assertNotNull(match, "Should match 'Card XXXX2468'")
        assertEquals("2468", match.groupValues[1])
    }

    @Test
    fun testCardMaskedPattern_lowercase() {
        val sms = "card xxxx9876 debited Rs.1000"
        val match = cardMaskedPattern.find(sms)
        assertNotNull(match, "Should match lowercase 'card xxxx9876'")
        assertEquals("9876", match.groupValues[1])
    }

    // ========== Pattern 4: "A/c XX1234" Tests ==========

    @Test
    fun testAccountPattern_ac() {
        val sms = "Debited from A/c XX5432 on 25-Dec"
        val match = accountPattern.find(sms)
        assertNotNull(match, "Should match 'A/c XX5432'")
        assertEquals("5432", match.groupValues[1])
    }

    @Test
    fun testAccountPattern_acct() {
        val sms = "Transaction on Acct XX6789 approved"
        val match = accountPattern.find(sms)
        assertNotNull(match, "Should match 'Acct XX6789'")
        assertEquals("6789", match.groupValues[1])
    }

    @Test
    fun testAccountPattern_account() {
        val sms = "Payment from Account XX1111 successful"
        val match = accountPattern.find(sms)
        assertNotNull(match, "Should match 'Account XX1111'")
        assertEquals("1111", match.groupValues[1])
    }

    // ========== Pattern 5: Full card with spaces Tests ==========

    @Test
    fun testFullCardSpaces_valid() {
        val input = "Card: 4532 1234 5678 9010"
        val match = fullCardSpacesPattern.find(input)
        assertNotNull(match, "Should match full card with spaces")
        assertEquals("4532", match.groupValues[1])
        assertEquals("1234", match.groupValues[2])
        assertEquals("5678", match.groupValues[3])
        assertEquals("9010", match.groupValues[4])
    }

    @Test
    fun testFullCardSpaces_visa() {
        val input = "Visa: 4111 1111 1111 1111"
        val match = fullCardSpacesPattern.find(input)
        assertNotNull(match, "Should match Visa card format")
        assertEquals("4111", match.groupValues[1])
    }

    // ========== Pattern 6: Full card without spaces Tests ==========

    @Test
    fun testFullCardNoSpaces_valid() {
        val input = "Card number: 5425233430109903"
        val match = fullCardNoSpacesPattern.find(input)
        assertNotNull(match, "Should match 16-digit card number")
        assertEquals("5425233430109903", match.groupValues[1])
    }

    @Test
    fun testFullCardNoSpaces_shouldNotMatchPartial() {
        val input = "Transaction ID: 123456789012345" // 15 digits
        val match = fullCardNoSpacesPattern.find(input)
        assertNull(match, "Should not match 15-digit number")
    }

    // ========== Pattern 7: Full card with dashes Tests ==========

    @Test
    fun testFullCardDashes_valid() {
        val input = "Card: 4532-1234-5678-9010"
        val match = fullCardDashesPattern.find(input)
        assertNotNull(match, "Should match card with dashes")
        assertEquals("4532", match.groupValues[1])
        assertEquals("1234", match.groupValues[2])
        assertEquals("5678", match.groupValues[3])
        assertEquals("9010", match.groupValues[4])
    }

    // ========== Combined Pattern Tests ==========

    @Test
    fun testCombinedPattern_cardXX() {
        val sms = "Spent Rs.500 on Card XX1234"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Combined pattern should match 'Card XX1234'")
        assertEquals("1234", match.groupValues[1])
    }

    @Test
    fun testCombinedPattern_cardEnding() {
        val sms = "Payment from card ending 5678"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Combined pattern should match 'card ending 5678'")
        assertEquals("5678", match.groupValues[1])
    }

    @Test
    fun testCombinedPattern_cardNo() {
        val sms = "Transaction on Card No 9012"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Combined pattern should match 'Card No 9012'")
        assertEquals("9012", match.groupValues[1])
    }

    @Test
    fun testCombinedPattern_acct() {
        val sms = "Debited from Acct 3456"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Combined pattern should match 'Acct 3456'")
        assertEquals("3456", match.groupValues[1])
    }

    // ========== Real-world Indian Bank SMS Tests ==========

    @Test
    fun testHDFCBankSMS() {
        val sms = "Rs.399.00 spent on HDFC Bank Card XX1234 at AMAZON PRIME on 21-Dec-24. Avl Bal: Rs.10,832.00"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from HDFC SMS")
        assertEquals("1234", match.groupValues[1])
    }

    @Test
    fun testICICIBankSMS() {
        val sms = "INR 599.00 debited from Card XX5678 at NETFLIX on 22-Dec-24. Available limit: INR 44,803.00"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from ICICI SMS")
        assertEquals("5678", match.groupValues[1])
    }

    @Test
    fun testSBICardSMS() {
        val sms = "Rs.2,100.00 spent on SBI Card XX9012 at TATA POWER on 23-Dec-24. Available credit: Rs.44,301.00"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from SBI SMS")
        assertEquals("9012", match.groupValues[1])
    }

    @Test
    fun testAxisBankSMS() {
        val sms = "Dear Customer, Rs.1500.00 debited from your Axis Bank Card ending 3456 at SWIGGY on 24-Dec-24"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from Axis SMS")
        assertEquals("3456", match.groupValues[1])
    }

    @Test
    fun testKotakBankSMS() {
        val sms = "Transaction of Rs.750 on Kotak Card XXXX7890 at ZOMATO approved. Avl limit: Rs.25,000"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from Kotak SMS")
        assertEquals("7890", match.groupValues[1])
    }

    @Test
    fun testCitiBankSMS() {
        val sms = "Your Citi Card A/c XX2468 has been debited with Rs.2500 at FLIPKART on 25-Dec-24"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from Citi SMS")
        assertEquals("2468", match.groupValues[1])
    }

    @Test
    fun testAmexSMS() {
        val sms = "American Express Card ending in 1357 charged Rs.5000 at APPLE STORE"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should extract card from Amex SMS")
        assertEquals("1357", match.groupValues[1])
    }

    // ========== Edge Cases and Negative Tests ==========

    @Test
    fun testNoCardNumber() {
        val sms = "Your payment of Rs.500 was successful"
        val match = combinedCardPattern.find(sms)
        assertNull(match, "Should not match when no card number present")
    }

    @Test
    fun testInvalidCardDigits_tooFew() {
        val sms = "Card XX123 used" // Only 3 digits
        val match = combinedCardPattern.find(sms)
        assertNull(match, "Should not match card with only 3 digits")
    }

    @Test
    fun testInvalidCardDigits_tooMany() {
        val sms = "Card XX12345 used" // 5 digits
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should match but only capture first 4 digits")
        assertEquals("1234", match.groupValues[1])
    }

    @Test
    fun testMultipleCardNumbers() {
        val sms = "Transfer from Card XX1234 to Card XX5678 successful"
        val matches = combinedCardPattern.findAll(sms).toList()
        assertEquals(2, matches.size, "Should find both card numbers")
        assertEquals("1234", matches[0].groupValues[1])
        assertEquals("5678", matches[1].groupValues[1])
    }

    @Test
    fun testCardNumberWithExtraSpaces() {
        val sms = "Payment from Card    XX1234 approved"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should handle extra spaces")
        assertEquals("1234", match.groupValues[1])
    }

    @Test
    fun testCardNumberAtStartOfMessage() {
        val sms = "Card XX9999 debited Rs.100"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should match card at start of message")
        assertEquals("9999", match.groupValues[1])
    }

    @Test
    fun testCardNumberAtEndOfMessage() {
        val sms = "Transaction successful on Card XX8888"
        val match = combinedCardPattern.find(sms)
        assertNotNull(match, "Should match card at end of message")
        assertEquals("8888", match.groupValues[1])
    }
}




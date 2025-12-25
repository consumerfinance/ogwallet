package dev.consumerfinance.ogwallet.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for detecting credit card networks (Visa, Mastercard, RuPay, etc.)
 * based on card number patterns used in India.
 */
class CreditCardNetworkDetectionTest {

    companion object {
        // Visa: Starts with 4
        val visaPattern = Regex("^4\\d{15}$")
        
        // Mastercard: Starts with 51-55 or 2221-2720
        val mastercardPattern = Regex("^(5[1-5]\\d{14}|2(22[1-9]|2[3-9]\\d|[3-6]\\d{2}|7[01]\\d|720)\\d{12})$")
        
        // RuPay: Starts with 60, 65, 81, 82, 508
        val rupayPattern = Regex("^(60|65|81|82|508)\\d{13,16}$")
        
        // American Express: Starts with 34 or 37 (15 digits)
        val amexPattern = Regex("^3[47]\\d{13}$")
        
        // Diners Club: Starts with 36 or 38 (14 digits)
        val dinersPattern = Regex("^3[68]\\d{12}$")
        
        // Discover: Starts with 6011, 622126-622925, 644-649, 65
        val discoverPattern = Regex("^(6011|65|64[4-9]|622(1(2[6-9]|[3-9]\\d)|[2-8]\\d{2}|9([01]\\d|2[0-5])))\\d{12}$")
        
        fun detectCardNetwork(cardNumber: String): String {
            val cleaned = cardNumber.replace(Regex("\\s|-"), "")
            return when {
                visaPattern.matches(cleaned) -> "Visa"
                mastercardPattern.matches(cleaned) -> "Mastercard"
                rupayPattern.matches(cleaned) -> "RuPay"
                amexPattern.matches(cleaned) -> "American Express"
                dinersPattern.matches(cleaned) -> "Diners Club"
                discoverPattern.matches(cleaned) -> "Discover"
                else -> "Unknown"
            }
        }
    }

    // ========== Visa Card Tests ==========
    
    @Test
    fun testVisaCard_valid() {
        val cardNumber = "4111111111111111"
        assertTrue(visaPattern.matches(cardNumber), "Should match Visa pattern")
        assertEquals("Visa", detectCardNetwork(cardNumber))
    }

    @Test
    fun testVisaCard_withSpaces() {
        val cardNumber = "4532 1234 5678 9010"
        assertEquals("Visa", detectCardNetwork(cardNumber))
    }

    @Test
    fun testVisaCard_withDashes() {
        val cardNumber = "4532-1234-5678-9010"
        assertEquals("Visa", detectCardNetwork(cardNumber))
    }

    // ========== Mastercard Tests ==========
    
    @Test
    fun testMastercard_51Series() {
        val cardNumber = "5105105105105100"
        assertTrue(mastercardPattern.matches(cardNumber), "Should match Mastercard 51 series")
        assertEquals("Mastercard", detectCardNetwork(cardNumber))
    }

    @Test
    fun testMastercard_55Series() {
        val cardNumber = "5555555555554444"
        assertTrue(mastercardPattern.matches(cardNumber), "Should match Mastercard 55 series")
        assertEquals("Mastercard", detectCardNetwork(cardNumber))
    }

    @Test
    fun testMastercard_2221Series() {
        val cardNumber = "2221000000000009"
        assertTrue(mastercardPattern.matches(cardNumber), "Should match Mastercard 2221 series")
        assertEquals("Mastercard", detectCardNetwork(cardNumber))
    }

    @Test
    fun testMastercard_2720Series() {
        val cardNumber = "2720999999999996"
        assertTrue(mastercardPattern.matches(cardNumber), "Should match Mastercard 2720 series")
        assertEquals("Mastercard", detectCardNetwork(cardNumber))
    }

    // ========== RuPay Card Tests ==========
    
    @Test
    fun testRuPay_60Series() {
        val cardNumber = "6076820000000000"
        assertTrue(rupayPattern.matches(cardNumber), "Should match RuPay 60 series")
        assertEquals("RuPay", detectCardNetwork(cardNumber))
    }

    @Test
    fun testRuPay_65Series() {
        val cardNumber = "6521450000000000"
        assertTrue(rupayPattern.matches(cardNumber), "Should match RuPay 65 series")
        assertEquals("RuPay", detectCardNetwork(cardNumber))
    }

    @Test
    fun testRuPay_81Series() {
        val cardNumber = "8171999900000000"
        assertTrue(rupayPattern.matches(cardNumber), "Should match RuPay 81 series")
        assertEquals("RuPay", detectCardNetwork(cardNumber))
    }

    @Test
    fun testRuPay_508Series() {
        val cardNumber = "5081234567890123"
        assertTrue(rupayPattern.matches(cardNumber), "Should match RuPay 508 series")
        assertEquals("RuPay", detectCardNetwork(cardNumber))
    }

    // ========== American Express Tests ==========
    
    @Test
    fun testAmex_34Series() {
        val cardNumber = "340000000000009"
        assertTrue(amexPattern.matches(cardNumber), "Should match Amex 34 series")
        assertEquals("American Express", detectCardNetwork(cardNumber))
    }

    @Test
    fun testAmex_37Series() {
        val cardNumber = "378282246310005"
        assertTrue(amexPattern.matches(cardNumber), "Should match Amex 37 series")
        assertEquals("American Express", detectCardNetwork(cardNumber))
    }

    // ========== Diners Club Tests ==========
    
    @Test
    fun testDiners_36Series() {
        val cardNumber = "36000000000000"
        assertTrue(dinersPattern.matches(cardNumber), "Should match Diners 36 series")
        assertEquals("Diners Club", detectCardNetwork(cardNumber))
    }

    @Test
    fun testDiners_38Series() {
        val cardNumber = "38000000000000"
        assertTrue(dinersPattern.matches(cardNumber), "Should match Diners 38 series")
        assertEquals("Diners Club", detectCardNetwork(cardNumber))
    }

    // ========== Edge Cases ==========
    
    @Test
    fun testUnknownCard() {
        val cardNumber = "9999999999999999"
        assertEquals("Unknown", detectCardNetwork(cardNumber))
    }

    @Test
    fun testInvalidLength() {
        val cardNumber = "411111111111" // Too short
        assertEquals("Unknown", detectCardNetwork(cardNumber))
    }
}


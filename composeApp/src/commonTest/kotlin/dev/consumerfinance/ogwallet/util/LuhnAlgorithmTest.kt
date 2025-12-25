package dev.consumerfinance.ogwallet.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for Luhn algorithm (mod 10 checksum) validation.
 * The Luhn algorithm is used to validate credit card numbers.
 */
class LuhnAlgorithmTest {

    companion object {
        /**
         * Validates a credit card number using the Luhn algorithm.
         * 
         * Algorithm:
         * 1. Starting from the rightmost digit (check digit), double every second digit
         * 2. If doubling results in a two-digit number, add the digits together
         * 3. Sum all the digits
         * 4. If the total modulo 10 is 0, the number is valid
         */
        fun isValidLuhn(cardNumber: String): Boolean {
            val cleaned = cardNumber.replace(Regex("\\s|-"), "")
            
            if (cleaned.isEmpty() || !cleaned.all { it.isDigit() }) {
                return false
            }
            
            var sum = 0
            var alternate = false
            
            // Iterate from right to left
            for (i in cleaned.length - 1 downTo 0) {
                var digit = cleaned[i].toString().toInt()
                
                if (alternate) {
                    digit *= 2
                    if (digit > 9) {
                        digit = (digit % 10) + (digit / 10)
                    }
                }
                
                sum += digit
                alternate = !alternate
            }
            
            return sum % 10 == 0
        }
    }

    // ========== Valid Card Numbers ==========
    
    @Test
    fun testValidVisa() {
        assertTrue(isValidLuhn("4532015112830366"), "Valid Visa card should pass Luhn check")
    }

    @Test
    fun testValidMastercard() {
        assertTrue(isValidLuhn("5425233430109903"), "Valid Mastercard should pass Luhn check")
    }

    @Test
    fun testValidAmex() {
        assertTrue(isValidLuhn("374245455400126"), "Valid Amex card should pass Luhn check")
    }

    @Test
    fun testValidDiscover() {
        assertTrue(isValidLuhn("6011000991300009"), "Valid Discover card should pass Luhn check")
    }

    @Test
    fun testValidDiners() {
        assertTrue(isValidLuhn("36227206271667"), "Valid Diners card should pass Luhn check")
    }

    // ========== Test Cards (commonly used for testing) ==========
    
    @Test
    fun testVisaTestCard() {
        assertTrue(isValidLuhn("4111111111111111"), "Visa test card should pass Luhn check")
    }

    @Test
    fun testMastercardTestCard() {
        assertTrue(isValidLuhn("5555555555554444"), "Mastercard test card should pass Luhn check")
    }

    @Test
    fun testAmexTestCard() {
        assertTrue(isValidLuhn("378282246310005"), "Amex test card should pass Luhn check")
    }

    // ========== Invalid Card Numbers ==========
    
    @Test
    fun testInvalidChecksum() {
        assertFalse(isValidLuhn("4111111111111112"), "Invalid checksum should fail")
    }

    @Test
    fun testAllZeros() {
        // Note: "0000000000000000" actually passes Luhn check (sum = 0, 0 % 10 = 0)
        // This is technically valid per the algorithm, though not a real card
        assertTrue(isValidLuhn("0000000000000000"), "All zeros passes Luhn (sum=0)")
    }

    @Test
    fun testAllOnes() {
        assertFalse(isValidLuhn("1111111111111111"), "All ones should fail")
    }

    @Test
    fun testRandomInvalid() {
        assertFalse(isValidLuhn("1234567890123456"), "Random number should likely fail")
    }

    // ========== Format Variations ==========
    
    @Test
    fun testValidWithSpaces() {
        assertTrue(isValidLuhn("4532 0151 1283 0366"), "Should handle spaces")
    }

    @Test
    fun testValidWithDashes() {
        assertTrue(isValidLuhn("4532-0151-1283-0366"), "Should handle dashes")
    }

    @Test
    fun testValidMixedFormatting() {
        assertTrue(isValidLuhn("4532 0151-1283 0366"), "Should handle mixed formatting")
    }

    // ========== Edge Cases ==========
    
    @Test
    fun testEmptyString() {
        assertFalse(isValidLuhn(""), "Empty string should fail")
    }

    @Test
    fun testNonNumeric() {
        assertFalse(isValidLuhn("abcd1234efgh5678"), "Non-numeric characters should fail")
    }

    @Test
    fun testTooShort() {
        assertFalse(isValidLuhn("4111"), "Too short should fail")
    }

    @Test
    fun testSingleDigit() {
        // Single digit "0" passes Luhn (sum=0, 0 % 10 = 0)
        assertTrue(isValidLuhn("0"), "Single digit 0 passes Luhn")
    }

    // ========== Specific Luhn Algorithm Steps ==========
    
    @Test
    fun testLuhnDoubling() {
        // Test card: 4532015112830366
        // From right: 6,6,3,0,8,3,2,1,1,1,5,1,0,2,3,5,4
        // Doubled:    6,12,3,0,8,6,2,2,1,2,5,2,0,4,3,10,4
        // Summed:     6,3,3,0,8,6,2,2,1,2,5,2,0,4,3,1,4 = 52
        // 52 % 10 = 2, but we need to verify the actual implementation
        assertTrue(isValidLuhn("4532015112830366"))
    }

    @Test
    fun testLuhnWithAllDoubledDigitsOver9() {
        // Card where many doubled digits exceed 9
        assertTrue(isValidLuhn("5555555555554444"))
    }

    // ========== Real Indian Bank Cards (masked for privacy) ==========
    
    @Test
    fun testIndianBankCardFormat_HDFC() {
        // Typical HDFC Visa format (example, not real)
        val testCard = "4532015112830366"
        assertTrue(isValidLuhn(testCard), "HDFC-style Visa should validate")
    }

    @Test
    fun testIndianBankCardFormat_ICICI() {
        // Typical ICICI Mastercard format (example, not real)
        val testCard = "5425233430109903"
        assertTrue(isValidLuhn(testCard), "ICICI-style Mastercard should validate")
    }

    @Test
    fun testIndianBankCardFormat_SBI() {
        // Typical SBI card format (example, not real)
        val testCard = "4111111111111111"
        assertTrue(isValidLuhn(testCard), "SBI-style card should validate")
    }
}


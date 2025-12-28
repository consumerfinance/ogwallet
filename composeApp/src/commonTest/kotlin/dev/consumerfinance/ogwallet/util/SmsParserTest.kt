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
        assertEquals("SHOPPING", result.category)
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
        assertEquals("ENTERTAINMENT", result.category)
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

    // ========== Real SMS Tests from sms.txt ==========

    @Test
    fun testRealSms_SwiggyDebit() {
        val sms = "Sent Rs.177.00\nFrom HDFC Bank A/C *5755\nTo SWIGGY\nOn 27/12/25\nRef 183965187977\nNot You?\nCall 18002586161/SMS BLOCK UPI to 7308080808"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Swiggy SMS")
        assertEquals(177.0, result.amount)
        assertEquals("5755", result.accountHandle)
        assertEquals("SWIGGY", result.merchantRaw)
        assertEquals("FOOD", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_AirtelBill() {
        val sms = "Spent INR 1072.62\nAxis Bank Card no. XX4567\n23-12-25 02:19:35 IST\nWWW AIRTEL\nAvl Limit: INR 163172.13\nNot you? SMS BLOCK 4567 to 919951860002"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Airtel SMS")
        assertEquals(1072.62, result.amount)
        assertEquals("4567", result.accountHandle)
        assertEquals("WWW AIRTEL", result.merchantRaw)
        assertEquals("BILLS", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_AmazonShopping() {
        val sms = "INR 834.00 spent using ICICI Bank Card XX9003 on 21-Dec-25 on AMAZON PAY IN E. Avl Limit: INR 75,812.00. If not you, call 1800 2662/SMS BLOCK 9003 to 9215676766."
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Amazon SMS")
        assertEquals(834.0, result.amount)
        assertEquals("9003", result.accountHandle)
        assertEquals("AMAZON PAY IN E", result.merchantRaw)
        assertEquals("SHOPPING", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_AmazonLargeAmount() {
        val sms = "INR 3,354.00 spent using ICICI Bank Card XX9003 on 21-Dec-25 on AMAZON PAY IN E. Avl Limit: INR 76,646.00. If not you, call 1800 2662/SMS BLOCK 9003 to 9215676766."
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse large Amazon SMS")
        assertEquals(3354.0, result.amount)
        assertEquals("9003", result.accountHandle)
        assertEquals("AMAZON PAY IN E", result.merchantRaw)
        assertEquals("SHOPPING", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_AxisBillPayment() {
        val sms = "Payment of INR 5470.67 has been received towards your Axis Bank Credit Card XX8308 on 20-12-25 - Axis Bank"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Axis bill payment SMS")
        assertEquals(5470.67, result.amount)
        assertEquals("8308", result.accountHandle)
        assertEquals("Axis Bank", result.merchantRaw)
        assertEquals("OTHER", result.category) // Bill payment, not fitting other categories
        assertEquals(TransactionType.CREDIT, result.transactionType)
    }

    @Test
    fun testRealSms_PersonalTransfer() {
        val sms = "Sent Rs.20.00\nFrom HDFC Bank A/C *5755\nTo Mohinder Singh And Sons\nOn 20/12/25\nRef 224949105905\nNot You?\nCall 18002586161/SMS BLOCK UPI to 7308080808"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse personal transfer SMS")
        assertEquals(20.0, result.amount)
        assertEquals("5755", result.accountHandle)
        assertEquals("Mohinder Singh And Sons", result.merchantRaw)
        assertEquals("OTHER", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_StorePurchase() {
        val sms = "Sent Rs.654.00\nFrom HDFC Bank A/C *5755\nTo GAINDA MULL HEMRAJ STORE\nOn 19/12/25\nRef 486483259690\nNot You?\nCall 18002586161/SMS BLOCK UPI to 7308080808"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse store purchase SMS")
        assertEquals(654.0, result.amount)
        assertEquals("5755", result.accountHandle)
        assertEquals("GAINDA MULL HEMRAJ STORE", result.merchantRaw)
        assertEquals("OTHER", result.category) // Store, but not specific category
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_HungryPointFood() {
        val sms = "Sent Rs.254.00\nFrom HDFC Bank A/C *5755\nTo HUNGRY POINT\nOn 17/12/25\nRef 259228769923\nNot You?\nCall 18002586161/SMS BLOCK UPI to 7308080808"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Hungry Point SMS")
        assertEquals(254.0, result.amount)
        assertEquals("5755", result.accountHandle)
        assertEquals("HUNGRY POINT", result.merchantRaw)
        assertEquals("FOOD", result.category) // Contains "food" implicitly, but parser checks for keywords
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_AppleMediaServices() {
        val sms = "UPI Mandate:\nSent Rs.179.00\nfrom HDFC Bank A/c 5755\nTo APPLE MEDIA SERVICES\n14/12/25\nRef 102215600573\nNot You? Call 18002586161/SMS BLOCK UPI to 7308080808"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Apple Media Services SMS")
        assertEquals(179.0, result.amount)
        assertEquals("5755", result.accountHandle)
        assertEquals("APPLE MEDIA SERVICES", result.merchantRaw)
        assertEquals("ENTERTAINMENT", result.category) // Apple, entertainment
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_ZomatoFood() {
        val sms = "Tasty choice! You've spent Rs. 276.42 at Zomato Limited with your BOBCARD One Credit Card ending in XX4067. Reward points served on the side. To dispute this payment, click: m.1crd.in/OneCrd/shcut"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Zomato SMS")
        assertEquals(276.42, result.amount)
        assertEquals("4067", result.accountHandle)
        assertEquals("Zomato Limited", result.merchantRaw)
        assertEquals("FOOD", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_MakeMyTripTravel() {
        val sms = "Dear Abhinav,\n\nThank you for booking with us. We are happy to inform you that your flight with booking ID NF91124367624954 is confirmed.\n\nYour Trip Details:\n\nDeparture Flight: Chandigarh-Bangalore | Date: 15 Dec 25 | Add-ons: IXC-BLR, 6E-6634(Meals: Chicken Junglee Sandwich + Bev, Veg Biryani + Beverage of choi)\n\nTotal amount paid: INR 20,668.00\n\nYou can manage your booking at https://mmyt.co/MMTRIP/XVYZk1kJ\n\nWe wish you a safe journey.\nTeam MakeMyTrip"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse MakeMyTrip SMS")
        assertEquals(20668.0, result.amount)
        assertEquals("Unknown", result.accountHandle) // No card number
        assertEquals("Team MakeMyTrip", result.merchantRaw)
        assertEquals("TRAVEL", result.category)
        assertEquals(TransactionType.DEBIT, result.transactionType)
    }

    @Test
    fun testRealSms_SwiggyRefund() {
        val sms = "Credit Alert!\nRs.232.00 credited to HDFC Bank A/c XX5755 on 26-11-25 from VPA upiswiggy@icici (UPI 108642890035)\n\n"
        val result = smsParser.parse(sms)

        assertNotNull(result, "Should parse Swiggy refund SMS")
        assertEquals(232.0, result.amount)
        assertEquals("5755", result.accountHandle)
        assertEquals("VPA upiswiggy@icici", result.merchantRaw)
        assertEquals("FOOD", result.category) // Contains swiggy
        assertEquals(TransactionType.CREDIT, result.transactionType)
    }
}


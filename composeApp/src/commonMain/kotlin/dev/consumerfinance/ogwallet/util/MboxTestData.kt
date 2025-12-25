package dev.consumerfinance.ogwallet.util

/**
 * Sample mbox data for testing the import functionality
 */
object MboxTestData {
    
    val sampleMbox = """
From noreply@bank.com Mon Dec 24 10:30:00 2024
From: noreply@bank.com
To: user@example.com
Subject: Transaction Alert - HDFC Bank
Date: Mon, 24 Dec 2024 10:30:00 +0530

Dear Customer,

A transaction of Rs.2,500.00 has been made on your HDFC Credit Card ending 1234 at AMAZON on 24-Dec-2024.

Available Credit Limit: Rs.47,500.00

If you did not make this transaction, please contact us immediately.

Regards,
HDFC Bank

From noreply@icici.com Mon Dec 24 14:15:00 2024
From: noreply@icici.com
To: user@example.com
Subject: Payment Confirmation - ICICI Bank
Date: Mon, 24 Dec 2024 14:15:00 +0530

Dear Valued Customer,

Your payment of Rs.5,000.00 has been successfully processed for your ICICI Credit Card ending 5678.

Transaction Date: 24-Dec-2024
Payment Mode: Net Banking

Thank you for banking with us.

Best Regards,
ICICI Bank

From bills@swiggy.com Mon Dec 24 20:45:00 2024
From: bills@swiggy.com
To: user@example.com
Subject: Your Swiggy Order Receipt
Date: Mon, 24 Dec 2024 20:45:00 +0530

Hi there!

Thank you for ordering from Swiggy!

Order Details:
Total Amount: Rs.450.00
Payment Method: Card ending 1234
Order ID: SWG123456789

Enjoy your meal!

Team Swiggy

From statement@axis.com Mon Dec 23 09:00:00 2024
From: statement@axis.com
To: user@example.com
Subject: Credit Card Bill Generated - Axis Bank
Date: Mon, 23 Dec 2024 09:00:00 +0530

Dear Customer,

Your Axis Bank Credit Card statement for card ending 9876 has been generated.

Statement Date: 23-Dec-2024
Total Amount Due: Rs.12,450.00
Minimum Amount Due: Rs.1,245.00
Due Date: 10-Jan-2025

Please ensure timely payment to avoid late fees.

Regards,
Axis Bank

From alerts@flipkart.com Mon Dec 22 16:30:00 2024
From: alerts@flipkart.com
To: user@example.com
Subject: Order Confirmation - Flipkart
Date: Mon, 22 Dec 2024 16:30:00 +0530

Hello!

Your order has been confirmed!

Order Total: Rs.3,299.00
Payment: Paid via card ending 1234
Delivery Expected: 25-Dec-2024

Track your order: https://flipkart.com/track

Happy Shopping!
Flipkart Team
""".trimIndent()
}


# Microsoft Forms Template for Credit Card Offers

Use this template to create your Microsoft Form for crowdsourcing credit card offers and benefits.

## Form Title
**Credit Card Offers & Benefits Submission**

## Form Description
Help the community by sharing credit card offers and benefits you've discovered! Your submissions will be reviewed and added to the OGWallet app to help others maximize their credit card rewards.

---

## Questions

### 1. Card Name
**Type:** Text (Short answer)  
**Required:** Yes  
**Placeholder:** e.g., "Chase Sapphire Preferred"  
**Description:** What is the name of the credit card?

---

### 2. Bank/Issuer Name
**Type:** Text (Short answer)  
**Required:** Yes  
**Placeholder:** e.g., "Chase", "American Express", "Citi"  
**Description:** Which bank or financial institution issues this card?

---

### 3. Offer Title
**Type:** Text (Short answer)  
**Required:** Yes  
**Placeholder:** e.g., "5x Points on Dining"  
**Description:** Give this offer a short, descriptive title

---

### 4. Offer Description
**Type:** Text (Long answer)  
**Required:** Yes  
**Placeholder:** e.g., "Earn 5 points per dollar spent at restaurants and food delivery services"  
**Description:** Provide detailed information about this offer or benefit

---

### 5. Category
**Type:** Choice (Single select)  
**Required:** Yes  
**Options:**
- 🍽️ Dining
- ✈️ Travel
- 🛍️ Shopping
- ⛽ Gas
- 🛒 Groceries
- 🎬 Entertainment
- 💳 Other

**Description:** Which category does this offer apply to?

---

### 6. Expiry Date
**Type:** Date  
**Required:** No  
**Description:** When does this offer expire? (Leave blank if permanent)

---

### 7. Minimum Spend Requirement
**Type:** Number  
**Required:** No  
**Placeholder:** e.g., "50"  
**Description:** Is there a minimum spend required to qualify for this offer? (in dollars)

---

### 8. Maximum Benefit Cap
**Type:** Number  
**Required:** No  
**Placeholder:** e.g., "500"  
**Description:** Is there a maximum benefit amount? (in dollars)

---

### 9. Benefit Value
**Type:** Text (Short answer)  
**Required:** No  
**Placeholder:** e.g., "5x points", "3% cashback", "$100 credit"  
**Description:** What is the value of this benefit?

---

### 10. Additional Terms & Conditions
**Type:** Text (Long answer)  
**Required:** No  
**Description:** Any important terms, conditions, or restrictions?

---

### 11. Your Email (Optional)
**Type:** Email  
**Required:** No  
**Description:** Provide your email if you'd like us to contact you for verification or updates (optional)

---

### 12. How did you learn about this offer?
**Type:** Choice (Single select)  
**Required:** No  
**Options:**
- Bank website
- Email from bank
- Credit card statement
- Social media
- Friend/Family
- Other

---

## Form Settings

### Notifications
- ✅ Get email notification of each response
- ✅ Get email summary of all responses (Daily)

### Response Options
- ✅ Accept responses
- ✅ Allow one response per person
- ❌ Shuffle questions (Keep in order)
- ✅ Show progress bar

### Customization
- **Theme:** Choose a professional theme (blue/green recommended)
- **Header Image:** Optional - Add a credit card themed image

---

## After Creating the Form

1. **Share the form:**
   - Get the shareable link
   - Share on social media, forums, or with your community
   - Consider creating a QR code for easy access

2. **Export responses:**
   - Go to "Responses" tab
   - Click "Open in Excel" to download responses
   - Use `process_excel.py` to import into OGWallet

3. **Set up automation (Optional):**
   - Use Microsoft Power Automate to trigger processing
   - Or use Microsoft Graph API for real-time processing

---

## Sample Submission

Here's an example of what a completed submission might look like:

- **Card Name:** Chase Sapphire Preferred
- **Bank Name:** Chase
- **Offer Title:** 5x Points on Dining
- **Description:** Earn 5 points per dollar spent at restaurants and food delivery services. This is a permanent benefit of the card.
- **Category:** Dining
- **Expiry Date:** (blank - permanent)
- **Min Spend:** (blank)
- **Max Benefit:** (blank)
- **Benefit Value:** 5x points
- **Terms:** Must use card for payment. Points post within 2 billing cycles.
- **Email:** user@example.com
- **Source:** Bank website

---

## Tips for Quality Submissions

1. **Be specific:** Include exact percentages, point values, or dollar amounts
2. **Verify information:** Check the bank's official website
3. **Include expiry dates:** If the offer is time-limited
4. **Note restrictions:** Mention any important terms or conditions
5. **Update regularly:** Submit updates if offers change

---

## Questions?

If you have questions about submitting offers, please contact the OGWallet team or check the documentation.

Thank you for contributing to the community! 🎉


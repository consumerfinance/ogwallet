# Credit Card Data Processing Setup Guide

This guide explains how to set up the credit card benefits and offers data processing system using Microsoft Forms, Python server, and Git submodules.

## Architecture Overview

```
┌─────────────────────┐
│  Microsoft Forms    │  ← Users submit credit card offers
│  (Crowdsourcing)    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   Python Server     │  ← Processes and validates data
│   (FastAPI)         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   Git Repository    │  ← Stores JSON data
│   (ogwallet-data)   │
└──────────┬──────────┘
           │ (git submodule)
           ▼
┌─────────────────────┐
│   Kotlin App        │  ← Loads and displays data
│   (OGWallet)        │
└─────────────────────┘
```

## Step 1: Create Microsoft Form

### Form Fields

Create a new Microsoft Form with these questions:

1. **Card Name** (Text)
   - Example: "Chase Sapphire Preferred"

2. **Bank Name** (Text)
   - Example: "Chase"

3. **Offer Title** (Text)
   - Example: "5x Points on Dining"

4. **Offer Description** (Long text)
   - Example: "Earn 5 points per dollar spent at restaurants and food delivery"

5. **Category** (Choice - Single select)
   - Dining
   - Travel
   - Shopping
   - Gas
   - Groceries
   - Entertainment
   - Other

6. **Expiry Date** (Date - Optional)
   - When does this offer expire?

7. **Minimum Spend** (Number - Optional)
   - Minimum spend required to qualify

8. **Maximum Benefit** (Number - Optional)
   - Maximum benefit amount (e.g., $500 cap)

9. **Email** (Email - Optional)
   - For verification purposes

### Share the Form

Share the form link with your community to crowdsource credit card offers.

## Step 2: Set Up Python Server

### Install Dependencies

```bash
cd data-server
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### Configure Environment

```bash
cp .env.example .env
```

Edit `.env`:
```env
# Option 1: Use Microsoft Graph API (requires Azure AD app)
FORMS_API_KEY=your_api_key_here
FORMS_FORM_ID=your_form_id_here

# Option 2: Use Excel export (easier for testing)
# Leave above blank and use process_excel.py script

DATA_REPO_PATH=../ogwallet-data
DATA_REPO_URL=https://github.com/yourusername/ogwallet-data.git
AUTO_COMMIT=true
```

### Initialize Data Repository

```bash
# Create sample data
python init_sample_data.py
```

This creates the `ogwallet-data` directory with sample offers and benefits.

## Step 3: Process Form Responses

### Option A: Using Excel Export (Recommended for Testing)

1. Go to your Microsoft Form
2. Click "Responses" tab
3. Click "Open in Excel"
4. Download the Excel file
5. Process it:

```bash
python process_excel.py path/to/responses.xlsx
```

### Option B: Using Microsoft Graph API

1. Register an app in Azure AD
2. Grant `Forms.Read.All` permission
3. Get API key and Form ID
4. Configure in `.env`
5. Run the server:

```bash
uvicorn main:app --reload
```

6. Trigger processing:

```bash
curl -X POST http://localhost:8000/process-forms
```

## Step 4: Set Up Git Submodule

### Create Data Repository on GitHub

```bash
cd ogwallet-data
git remote add origin https://github.com/yourusername/ogwallet-data.git
git push -u origin main
```

### Add as Submodule to OGWallet

```bash
cd /path/to/OGWallet
git submodule add https://github.com/yourusername/ogwallet-data.git composeApp/src/commonMain/resources/data
git submodule update --init --recursive
git commit -m "Add credit card data submodule"
```

### Update Submodule

When new data is available:

```bash
cd composeApp/src/commonMain/resources/data
git pull origin main
cd ../../../..
git add composeApp/src/commonMain/resources/data
git commit -m "Update credit card data"
```

## Step 5: Load Data in Kotlin App

The app already includes `CreditCardDataLoader.kt` which loads data from the git submodule.

To load from actual JSON file (once submodule is set up):

```kotlin
// Update CreditCardDataLoader.kt
fun loadData(): CreditCardDataRepository {
    val jsonString = this::class.java.classLoader
        ?.getResource("data/data.json")
        ?.readText()
        ?: return getSampleData()
    
    return json.decodeFromString<CreditCardDataRepository>(jsonString)
}
```

## Step 6: Automate with GitHub Actions

Create `.github/workflows/update-data.yml`:

```yaml
name: Update Credit Card Data

on:
  schedule:
    - cron: '0 */6 * * *'  # Every 6 hours
  workflow_dispatch:

jobs:
  update-data:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          submodules: true
      
      - uses: actions/setup-python@v4
        with:
          python-version: '3.11'
      
      - name: Install dependencies
        run: |
          cd data-server
          pip install -r requirements.txt
      
      - name: Process forms
        env:
          FORMS_API_KEY: ${{ secrets.FORMS_API_KEY }}
          FORMS_FORM_ID: ${{ secrets.FORMS_FORM_ID }}
        run: |
          cd data-server
          python -c "
          import asyncio
          from main import process_forms
          asyncio.run(process_forms())
          "
      
      - name: Commit and push
        run: |
          cd ogwallet-data
          git config user.name 'GitHub Actions'
          git config user.email 'actions@github.com'
          git add .
          git commit -m 'Auto-update credit card data' || exit 0
          git push
```

## API Endpoints

Once the server is running:

- `GET /` - Health check
- `GET /stats` - Processing statistics
- `POST /process-forms` - Process new form responses
- `GET /offers?category=dining&active_only=true` - Get offers
- `GET /benefits?card_name=Chase%20Sapphire` - Get benefits

## Data Schema

See `data-server/models.py` for complete schema.

## Troubleshooting

### Data not loading in app
- Check that git submodule is initialized: `git submodule status`
- Verify JSON file exists: `ls composeApp/src/commonMain/resources/data/data.json`

### Forms processing fails
- Check API credentials in `.env`
- Try Excel export method instead
- Check logs for error messages

### Git push fails
- Ensure you have write access to the data repository
- Check git credentials: `git config --list`

## Contributing

To contribute new offers:
1. Fill out the Microsoft Form
2. Data will be processed and added to the repository
3. App will load new data on next update

## License

MIT


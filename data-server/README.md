# OGWallet Data Processing Server

Python server for processing crowdsourced credit card benefits and offers data from Microsoft Forms.

## Features

- 📝 **Microsoft Forms Integration** - Fetch responses from Microsoft Forms or process Excel exports
- 🔄 **Automated Processing** - Convert form responses to structured JSON data
- 📦 **Git Submodule Management** - Automatically commit and push data updates
- 🚀 **REST API** - Expose data via FastAPI endpoints
- ✅ **Data Validation** - Pydantic models ensure data quality

## Architecture

```
Microsoft Forms → Python Server → Git Repository → Kotlin App
                      ↓
                  Validation
                  Processing
                  Deduplication
```

## Setup

### 1. Install Dependencies

```bash
cd data-server
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Run the Server

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

The server will be available at `http://localhost:8000`

## API Endpoints

### Health Check
```
GET /
```

### Get Statistics
```
GET /stats
```

### Process Forms
```
POST /process-forms
```
Fetches new responses from Microsoft Forms and updates the data repository.

### Get Offers
```
GET /offers?category=dining&active_only=true
```

### Get Benefits
```
GET /benefits?card_name=Chase%20Sapphire
```

## Microsoft Forms Setup

### Option 1: Using Microsoft Graph API

1. Register an app in Azure AD
2. Grant permissions: `Forms.Read.All`
3. Get API key and Form ID
4. Configure in `.env`

### Option 2: Using Excel Export (Recommended for Testing)

1. Create a Microsoft Form with these fields:
   - Card Name (Text)
   - Bank Name (Text)
   - Offer Title (Text)
   - Offer Description (Long text)
   - Category (Choice: Dining, Travel, Shopping, Gas, Groceries, Entertainment, Other)
   - Expiry Date (Date)
   - Minimum Spend (Number)
   - Benefit Value (Text)
   - Email (Email)

2. Export responses to Excel

3. Process the Excel file:
```python
from forms_processor import FormsProcessor
processor = FormsProcessor("", "")
responses = processor.process_excel_export("path/to/responses.xlsx")
```

## Git Submodule Integration

### Initialize Data Repository

```bash
# Create a new repository for data
mkdir ogwallet-data
cd ogwallet-data
git init
git remote add origin https://github.com/yourusername/ogwallet-data.git
```

### Add as Submodule to Main App

```bash
cd /path/to/OGWallet
git submodule add https://github.com/yourusername/ogwallet-data.git composeApp/src/commonMain/resources/data
git submodule update --init --recursive
```

### Update Submodule

```bash
cd composeApp/src/commonMain/resources/data
git pull origin main
cd ../../../..
git add composeApp/src/commonMain/resources/data
git commit -m "Update credit card data"
```

## Data Schema

See `models.py` for complete schema definitions.

### CreditCardOffer
```json
{
  "id": "unique-id",
  "title": "5x Points on Dining",
  "description": "Earn 5 points per dollar at restaurants",
  "card_name": "Chase Sapphire Preferred",
  "bank_name": "Chase",
  "category": "dining",
  "expiry_date": "2024-12-31",
  "emoji": "🍽️",
  "gradient_colors": ["#f97316", "#ef4444"],
  "is_active": true
}
```

## Automated Workflows

You can set up GitHub Actions to automatically process forms on a schedule:

```yaml
# .github/workflows/process-forms.yml
name: Process Forms Data
on:
  schedule:
    - cron: '0 */6 * * *'  # Every 6 hours
  workflow_dispatch:

jobs:
  process:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-python@v4
        with:
          python-version: '3.11'
      - run: pip install -r data-server/requirements.txt
      - run: python data-server/process_forms.py
```

## Development

### Run Tests
```bash
pytest
```

### Format Code
```bash
black .
```

### Type Checking
```bash
mypy .
```

## License

MIT


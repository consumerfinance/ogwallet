"""
Simple test to verify the data server setup
"""
import json
from pathlib import Path
from datetime import datetime

# Create sample data structure
sample_data = {
    "offers": [
        {
            "id": "offer-001",
            "title": "5x Points on Dining",
            "description": "Earn 5 points per dollar spent at restaurants and food delivery",
            "card_name": "Chase Sapphire Preferred",
            "bank_name": "Chase",
            "category": "dining",
            "expiry_date": "2024-12-31",
            "emoji": "🍽️",
            "gradient_colors": ["#f97316", "#ef4444"],
            "is_active": True,
            "upvotes": 45,
            "downvotes": 2
        },
        {
            "id": "offer-002",
            "title": "3% Cash Back on Gas",
            "description": "Get 3% cash back on all gas station purchases",
            "card_name": "Citi Custom Cash",
            "bank_name": "Citi",
            "category": "gas",
            "expiry_date": "2024-12-20",
            "emoji": "⛽",
            "gradient_colors": ["#06b6d4", "#0891b2"],
            "is_active": True,
            "max_benefit": 500.0,
            "upvotes": 38,
            "downvotes": 1
        },
        {
            "id": "offer-003",
            "title": "10% Back at Amazon",
            "description": "Limited time offer for Prime members",
            "card_name": "Amazon Prime Rewards",
            "bank_name": "Chase",
            "category": "shopping",
            "expiry_date": "2024-12-15",
            "emoji": "📦",
            "gradient_colors": ["#8b5cf6", "#ec4899"],
            "is_active": True,
            "min_spend": 50.0,
            "max_benefit": 200.0,
            "upvotes": 92,
            "downvotes": 5
        }
    ],
    "benefits": [],
    "redemption_options": [
        {
            "id": "redeem-001",
            "name": "Cash Back",
            "rate": "1 point = $0.01",
            "minimum_points": 2500,
            "emoji": "💵",
            "available": True
        },
        {
            "id": "redeem-002",
            "name": "Travel Rewards",
            "rate": "1 point = $0.015",
            "minimum_points": 5000,
            "emoji": "✈️",
            "available": True
        },
        {
            "id": "redeem-003",
            "name": "Gift Cards",
            "rate": "1 point = $0.012",
            "minimum_points": 2500,
            "emoji": "🎁",
            "available": True
        }
    ],
    "last_updated": datetime.now().isoformat(),
    "version": "1.0.0"
}

# Create directory structure
data_dir = Path("../ogwallet-data")
data_dir.mkdir(exist_ok=True)
(data_dir / "offers").mkdir(exist_ok=True)
(data_dir / "benefits").mkdir(exist_ok=True)
(data_dir / "redemptions").mkdir(exist_ok=True)

# Save main data file
with open(data_dir / "data.json", 'w') as f:
    json.dump(sample_data, f, indent=2)

# Save individual offer files
for offer in sample_data["offers"]:
    with open(data_dir / "offers" / f"{offer['id']}.json", 'w') as f:
        json.dump(offer, f, indent=2)

# Create README
readme_content = """# OGWallet Credit Card Data

This repository contains crowdsourced credit card benefits and offers data.

## Structure

- `offers/` - Credit card offers and promotions
- `benefits/` - Permanent credit card benefits
- `redemptions/` - Points/rewards redemption options
- `data.json` - Complete data repository

## Data Sources

Data is collected from:
- Microsoft Forms submissions
- Community contributions
- Official bank websites

## Usage

This repository is used as a git submodule in the OGWallet app.
"""

with open(data_dir / "README.md", 'w') as f:
    f.write(readme_content)

print("✅ Created data repository at ../ogwallet-data")
print(f"✅ Created {len(sample_data['offers'])} sample offers")
print(f"✅ Created {len(sample_data['redemption_options'])} redemption options")
print("🎉 Setup complete!")


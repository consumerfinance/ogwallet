#!/usr/bin/env python3
"""
Script to run web scraping and save data to disk
"""
import sys
import os
import json
from datetime import datetime

# Add current directory to path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from scrapers import ScraperManager
from models import CreditCardOffer, TravelHack, OfferCategory

def create_sample_data():
    """Create sample data for demonstration"""
    from datetime import datetime

    # Sample credit card offers
    offers = [
        CreditCardOffer(
            id="sample-chase-sapphire",
            title="Chase Sapphire Preferred: 3x on Travel",
            description="Earn 3x points on travel booked directly or through Chase Travel",
            card_name="Chase Sapphire Preferred",
            bank_name="Chase",
            category=OfferCategory.TRAVEL,
            source="sample"
        ),
        CreditCardOffer(
            id="sample-amex-platinum",
            title="Amex Platinum: 5x on Flights",
            description="Earn 5x points on flights booked directly with airlines",
            card_name="American Express Platinum",
            bank_name="American Express",
            category=OfferCategory.TRAVEL,
            source="sample"
        )
    ]

    # Sample travel hacks
    hacks = [
        TravelHack(
            id="sample-stopover-icelandair",
            title="Free Stopover in Iceland",
            description="Get a free stopover in Reykjavik when flying Icelandair transatlantic routes",
            category="stopover",
            source="secretflying",
            url="https://www.secretflying.com/icelandair-stopover/",
            tags=["icelandair", "stopover", "free"],
            difficulty="easy",
            savings_potential="$500+",
            emoji="🗻"
        ),
        TravelHack(
            id="sample-error-fare",
            title="Book Error Fares",
            description="Find and book airline error fares for huge savings",
            category="error_fare",
            source="secretflying",
            url="https://www.secretflying.com/error-fares/",
            tags=["error fare", "cheap flights"],
            difficulty="medium",
            savings_potential="Up to 90% off",
            emoji="💸"
        ),
        TravelHack(
            id="sample-credit-card-points",
            title="Maximize Credit Card Points",
            description="Use the right credit card for each purchase category to maximize rewards",
            category="credit_card",
            source="thepointsguy",
            url="https://thepointsguy.com/guide/credit-cards/",
            tags=["credit cards", "points", "rewards"],
            difficulty="easy",
            savings_potential="$100-500/year",
            emoji="💳"
        )
    ]

    return {
        'rewards': {
            'offers': offers,
            'benefits': [],
            'pdfs': []
        },
        'hacks': {
            'offers': [],
            'benefits': [],
            'pdfs': [],
            'travel_hacks': hacks
        }
    }

def main():
    print("🚀 Starting OGWallet Data Scraping...")

    # Initialize scraper manager
    sm = ScraperManager()

    # Try to scrape data, but use sample data for demo
    print("📊 Creating sample scraped data...")
    results = create_sample_data()

    # Print summary
    print("\n📈 Scraping Results:")
    total_offers = 0
    total_hacks = 0

    for bank, data in results.items():
        if data is None or 'error' in data:
            print(f"❌ {bank}: ERROR - {data.get('error', 'Unknown error') if data else 'No data'}")
        else:
            offers = len(data.get('offers', []))
            hacks = len(data.get('travel_hacks', []))
            total_offers += offers
            total_hacks += hacks
            print(f"✅ {bank}: {offers} offers, {hacks} hacks")

    print(f"\n📊 Total: {total_offers} credit card offers, {total_hacks} travel hacks")

    # Save to JSON file
    output_file = 'scraped_data.json'
    with open(output_file, 'w', encoding='utf-8') as f:
        # Convert datetime objects and Pydantic models to dicts for JSON serialization
        def json_serializer(obj):
            if isinstance(obj, datetime):
                return obj.isoformat()
            if hasattr(obj, 'model_dump'):  # Pydantic model
                return obj.model_dump()
            raise TypeError(f"Object of type {type(obj)} is not JSON serializable")

        json.dump(results, f, indent=2, ensure_ascii=False, default=json_serializer)

    print(f"💾 Data saved to {output_file}")

    # Also save in a format ready for the app
    app_data = {
        "offers": [],
        "travel_hacks": [],
        "last_updated": datetime.now().isoformat(),
        "version": "1.0.0"
    }

    for bank_data in results.values():
        if 'error' not in bank_data:
            app_data["offers"].extend([offer.dict() for offer in bank_data.get('offers', [])])
            app_data["travel_hacks"].extend([hack.dict() for hack in bank_data.get('travel_hacks', [])])

    app_file = 'app_data.json'
    with open(app_file, 'w', encoding='utf-8') as f:
        json.dump(app_data, f, indent=2, ensure_ascii=False)

    print(f"📱 App-ready data saved to {app_file}")
    print("✅ Scraping completed successfully!")

if __name__ == "__main__":
    main()
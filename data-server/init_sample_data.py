"""
Initialize the data repository with sample credit card offers and benefits
"""
import logging
from datetime import datetime, timedelta

from git_manager import GitDataManager
from models import (
    DataRepository,
    CreditCardOffer,
    CreditCardBenefit,
    RedemptionOption,
    OfferCategory,
    BenefitType
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def create_sample_offers():
    """Create sample credit card offers"""
    return [
        CreditCardOffer(
            id="offer-001",
            title="5x Points on Dining",
            description="Earn 5 points per dollar spent at restaurants and food delivery",
            card_name="Chase Sapphire Preferred",
            bank_name="Chase",
            category=OfferCategory.DINING,
            expiry_date=(datetime.now() + timedelta(days=90)).strftime("%Y-%m-%d"),
            emoji="🍽️",
            gradient_colors=["#f97316", "#ef4444"],
            min_spend=None,
            max_benefit=None,
            upvotes=45,
            downvotes=2
        ),
        CreditCardOffer(
            id="offer-002",
            title="3% Cash Back on Gas",
            description="Get 3% cash back on all gas station purchases",
            card_name="Citi Custom Cash",
            bank_name="Citi",
            category=OfferCategory.GAS,
            expiry_date=(datetime.now() + timedelta(days=60)).strftime("%Y-%m-%d"),
            emoji="⛽",
            gradient_colors=["#06b6d4", "#0891b2"],
            max_benefit=500.0,
            upvotes=38,
            downvotes=1
        ),
        CreditCardOffer(
            id="offer-003",
            title="10% Back at Amazon",
            description="Limited time offer for Prime members - 10% back on Amazon purchases",
            card_name="Amazon Prime Rewards",
            bank_name="Chase",
            category=OfferCategory.SHOPPING,
            expiry_date=(datetime.now() + timedelta(days=30)).strftime("%Y-%m-%d"),
            emoji="📦",
            gradient_colors=["#8b5cf6", "#ec4899"],
            min_spend=50.0,
            max_benefit=200.0,
            upvotes=92,
            downvotes=5
        ),
        CreditCardOffer(
            id="offer-004",
            title="2x Miles on Travel",
            description="Earn double miles on all travel purchases including flights, hotels, and car rentals",
            card_name="Capital One Venture",
            bank_name="Capital One",
            category=OfferCategory.TRAVEL,
            expiry_date=None,  # Permanent benefit
            emoji="✈️",
            gradient_colors=["#3b82f6", "#06b6d4"],
            is_permanent=True,
            upvotes=67,
            downvotes=3
        ),
        CreditCardOffer(
            id="offer-005",
            title="6% Back on Groceries",
            description="Earn 6% cash back on up to $6,000 in grocery purchases per year",
            card_name="Amex Blue Cash Preferred",
            bank_name="American Express",
            category=OfferCategory.GROCERIES,
            expiry_date=None,
            emoji="🛒",
            gradient_colors=["#10b981", "#059669"],
            max_benefit=360.0,
            upvotes=78,
            downvotes=4
        )
    ]


def create_sample_benefits():
    """Create sample credit card benefits"""
    return [
        CreditCardBenefit(
            id="benefit-001",
            card_name="Chase Sapphire Reserve",
            bank_name="Chase",
            benefit_type=BenefitType.LOUNGE_ACCESS,
            title="Priority Pass Lounge Access",
            description="Unlimited access to 1,300+ airport lounges worldwide",
            value="$469 value",
            annual_fee=550.0,
            is_permanent=True
        ),
        CreditCardBenefit(
            id="benefit-002",
            card_name="Amex Platinum",
            bank_name="American Express",
            benefit_type=BenefitType.POINTS,
            title="5x Points on Flights",
            description="Earn 5 Membership Rewards points per dollar on flights booked directly with airlines",
            value="5x points",
            category=OfferCategory.TRAVEL,
            annual_fee=695.0,
            is_permanent=True
        ),
        CreditCardBenefit(
            id="benefit-003",
            card_name="Discover it Cash Back",
            bank_name="Discover",
            benefit_type=BenefitType.CASHBACK,
            title="Rotating 5% Categories",
            description="Earn 5% cash back on rotating categories each quarter (up to $1,500)",
            value="5% cashback",
            conditions="Activation required each quarter",
            annual_fee=0.0,
            is_permanent=True
        )
    ]


def create_sample_redemptions():
    """Create sample redemption options"""
    return [
        RedemptionOption(
            id="redeem-001",
            name="Cash Back",
            rate="1 point = $0.01",
            minimum_points=2500,
            emoji="💵",
            available=True,
            card_networks=["Visa", "Mastercard", "Discover"]
        ),
        RedemptionOption(
            id="redeem-002",
            name="Travel Rewards",
            rate="1 point = $0.015",
            minimum_points=5000,
            emoji="✈️",
            available=True,
            card_networks=["Chase", "Capital One"]
        ),
        RedemptionOption(
            id="redeem-003",
            name="Gift Cards",
            rate="1 point = $0.012",
            minimum_points=2500,
            emoji="🎁",
            available=True,
            card_networks=["All"]
        ),
        RedemptionOption(
            id="redeem-004",
            name="Statement Credit",
            rate="1 point = $0.01",
            minimum_points=2500,
            emoji="💳",
            available=True,
            card_networks=["All"]
        ),
        RedemptionOption(
            id="redeem-005",
            name="Transfer to Partners",
            rate="1:1 transfer ratio",
            minimum_points=1000,
            emoji="🔄",
            available=True,
            card_networks=["Amex", "Chase", "Citi"]
        )
    ]


def main():
    """Initialize data repository with sample data"""
    logger.info("Initializing data repository with sample data...")
    
    # Initialize git manager
    git_manager = GitDataManager("../ogwallet-data")
    
    # Create data repository
    data_repo = DataRepository(
        offers=create_sample_offers(),
        benefits=create_sample_benefits(),
        redemption_options=create_sample_redemptions(),
        last_updated=datetime.now(),
        version="1.0.0"
    )
    
    # Save data
    git_manager.save_data(data_repo)
    
    # Commit
    git_manager.commit_and_push("Initialize with sample credit card data")
    
    logger.info(f"✅ Created {len(data_repo.offers)} offers")
    logger.info(f"✅ Created {len(data_repo.benefits)} benefits")
    logger.info(f"✅ Created {len(data_repo.redemption_options)} redemption options")
    logger.info("🎉 Data repository initialized successfully!")


if __name__ == "__main__":
    main()


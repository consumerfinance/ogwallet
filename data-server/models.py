"""
Data models for credit card benefits and offers
"""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
from enum import Enum


class OfferCategory(str, Enum):
    DINING = "dining"
    TRAVEL = "travel"
    SHOPPING = "shopping"
    GAS = "gas"
    GROCERIES = "groceries"
    ENTERTAINMENT = "entertainment"
    OTHER = "other"


class BenefitType(str, Enum):
    CASHBACK = "cashback"
    POINTS = "points"
    MILES = "miles"
    DISCOUNT = "discount"
    LOUNGE_ACCESS = "lounge_access"
    INSURANCE = "insurance"
    CONCIERGE = "concierge"


class CreditCardOffer(BaseModel):
    """Model for credit card offers from crowdsourced data"""
    id: str
    title: str
    description: str
    card_name: str
    bank_name: str
    category: OfferCategory
    expiry_date: Optional[str] = None
    terms: Optional[str] = None
    emoji: str = "💳"
    gradient_colors: List[str] = Field(default_factory=lambda: ["#3b82f6", "#06b6d4"])
    is_active: bool = True
    min_spend: Optional[float] = None
    max_benefit: Optional[float] = None
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now)
    source: str = "crowdsourced"  # crowdsourced, official, scraped
    upvotes: int = 0
    downvotes: int = 0


class CreditCardBenefit(BaseModel):
    """Model for credit card benefits"""
    id: str
    card_name: str
    bank_name: str
    benefit_type: BenefitType
    title: str
    description: str
    value: str  # e.g., "5x points", "3% cashback", "$100 credit"
    category: Optional[OfferCategory] = None
    conditions: Optional[str] = None
    annual_fee: Optional[float] = None
    is_permanent: bool = True
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now)


class RedemptionOption(BaseModel):
    """Model for points/rewards redemption options"""
    id: str
    name: str
    rate: str  # e.g., "1 point = $0.01"
    minimum_points: int
    emoji: str
    available: bool = True
    card_networks: List[str] = Field(default_factory=list)  # e.g., ["Visa", "Mastercard"]


class MicrosoftFormsResponse(BaseModel):
    """Model for parsing Microsoft Forms responses"""
    response_id: str
    submitted_at: datetime
    card_name: str
    bank_name: str
    offer_title: str
    offer_description: str
    category: str
    expiry_date: Optional[str] = None
    min_spend: Optional[float] = None
    benefit_value: Optional[str] = None
    is_verified: bool = False
    submitter_email: Optional[str] = None


class ScrapedPDF(BaseModel):
    """Model for scraped PDF documents"""
    url: str
    title: str
    content: str  # Extracted text content
    bank: str
    scraped_at: datetime = Field(default_factory=datetime.now)


class DataRepository(BaseModel):
    """Container for all credit card data"""
    offers: List[CreditCardOffer] = Field(default_factory=list)
    benefits: List[CreditCardBenefit] = Field(default_factory=list)
    redemption_options: List[RedemptionOption] = Field(default_factory=list)
    scraped_pdfs: List[ScrapedPDF] = Field(default_factory=list)
    travel_hacks: List[TravelHack] = Field(default_factory=list)
    last_updated: datetime = Field(default_factory=datetime.now)
    version: str = "1.0.0"


class TravelHack(BaseModel):
    """Model for travel hacks and tips"""
    id: str
    title: str
    description: str
    category: str  # e.g., "stopover", "error_fare", "credit_card", "booking_hack"
    source: str
    url: Optional[str] = None
    tags: List[str] = Field(default_factory=list)
    difficulty: str = "easy"  # easy, medium, hard
    savings_potential: Optional[str] = None
    emoji: str = "✈️"
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now)
    upvotes: int = 0
    downvotes: int = 0


class ProcessingStats(BaseModel):
    """Statistics for data processing"""
    total_responses: int = 0
    processed: int = 0
    failed: int = 0
    duplicates: int = 0
    last_run: Optional[datetime] = None


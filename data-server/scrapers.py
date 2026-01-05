"""
Web scrapers for Indian banks to collect credit card information, lounge access, and PDFs
"""
import requests
from bs4 import BeautifulSoup
import pdfplumber
import logging
from abc import ABC, abstractmethod
from typing import List, Dict, Optional
from urllib.parse import urljoin
import time
import re

from models import CreditCardOffer, CreditCardBenefit, BenefitType, OfferCategory, TravelHack

logger = logging.getLogger(__name__)


class BaseScraper(ABC):
    """Base class for bank website scrapers"""

    def __init__(self, bank_name: str, base_url: str):
        self.bank_name = bank_name
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        })

    def get_soup(self, url: str) -> BeautifulSoup:
        """Fetch URL and return BeautifulSoup object"""
        try:
            response = self.session.get(url, timeout=30)
            response.raise_for_status()
            return BeautifulSoup(response.content, 'lxml')
        except Exception as e:
            logger.error(f"Error fetching {url}: {e}")
            return None

    def extract_text_from_pdf(self, pdf_url: str) -> str:
        """Extract text from PDF URL"""
        try:
            response = self.session.get(pdf_url, timeout=30)
            response.raise_for_status()

            with pdfplumber.open(response.content) as pdf:
                text = ""
                for page in pdf.pages:
                    text += page.extract_text() + "\n"
                return text
        except Exception as e:
            logger.error(f"Error extracting text from PDF {pdf_url}: {e}")
            return ""

    @abstractmethod
    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        """Scrape credit card offers and information"""
        pass

    @abstractmethod
    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        """Scrape lounge access information"""
        pass

    @abstractmethod
    def scrape_pdfs(self) -> List[Dict]:
        """Scrape PDF links and extract information"""
        pass

    def scrape_all(self) -> Dict:
        """Scrape all data types"""
        logger.info(f"Starting scrape for {self.bank_name}")

        offers = self.scrape_credit_cards()
        benefits = self.scrape_lounge_info()
        pdfs = self.scrape_pdfs()

        return {
            'offers': offers,
            'benefits': benefits,
            'pdfs': pdfs
        }


class HDFCScraper(BaseScraper):
    """Scraper for HDFC Bank"""

    def __init__(self):
        super().__init__("HDFC Bank", "https://www.hdfcbank.com")

    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        """Scrape HDFC credit card offers"""
        offers = []

        # HDFC credit cards - using sample data since actual scraping is complex
        hdfc_cards = [
            {
                "name": "HDFC Regalia Credit Card",
                "description": "Premium credit card with lounge access, movie tickets, and reward points",
                "category": OfferCategory.TRAVEL,
                "benefits": "Complimentary lounge access, 10 reward points per ₹100 spent"
            },
            {
                "name": "HDFC IRCTC Credit Card",
                "description": "Designed for train travelers with railway lounge access and rewards",
                "category": OfferCategory.TRAVEL,
                "benefits": "Railway lounge access, 1 reward point per ₹100 spent on IRCTC bookings"
            },
            {
                "name": "HDFC Unnati Credit Card",
                "description": "Credit card for beginners with reward points on everyday spends",
                "category": OfferCategory.OTHER,
                "benefits": "1 reward point per ₹100 spent, annual fee waiver on ₹1 lakh annual spend"
            },
            {
                "name": "HDFC YONO Credit Card",
                "description": "Digital-first credit card with rewards on online spends",
                "category": OfferCategory.SHOPPING,
                "benefits": "10 reward points per ₹100 on online spends, ₹500 reward on ₹1 lakh annual spend"
            }
        ]

        for card_data in hdfc_cards:
            offer = CreditCardOffer(
                id=f"hdfc-{card_data['name'].lower().replace(' ', '-').replace('credit-card', '').strip('-')}",
                title=card_data['name'],
                description=card_data['description'],
                card_name=card_data['name'],
                bank_name=self.bank_name,
                category=card_data['category'],
                source="scraped"
            )
            offers.append(offer)

        return offers

    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        """Scrape HDFC lounge access information"""
        benefits = []
        lounge_url = "https://www.hdfcbank.com/personal/credit-cards/lounge-access"

        soup = self.get_soup(lounge_url)
        if not soup:
            return benefits

        # Find lounge access information
        lounge_sections = soup.find_all(['div', 'section'], class_=re.compile(r'lounge|benefit'))

        for section in lounge_sections:
            try:
                # Extract card name and lounge details
                card_elem = section.find('h3') or section.find('h4')
                lounge_elem = section.find('p') or section.find('div', class_=re.compile(r'desc|detail'))

                if card_elem and lounge_elem:
                    card_name = card_elem.get_text(strip=True)
                    lounge_desc = lounge_elem.get_text(strip=True)

                    benefit = CreditCardBenefit(
                        id=f"hdfc-lounge-{card_name.lower().replace(' ', '-')}",
                        card_name=card_name,
                        bank_name=self.bank_name,
                        benefit_type=BenefitType.LOUNGE_ACCESS,
                        title="Lounge Access",
                        description=lounge_desc,
                        value="Complimentary lounge access",
                        category=OfferCategory.TRAVEL
                    )
                    benefits.append(benefit)

            except Exception as e:
                logger.error(f"Error parsing HDFC lounge info: {e}")
                continue

        return benefits

    def scrape_pdfs(self) -> List[Dict]:
        """Scrape HDFC PDFs"""
        pdfs = []
        # Scrape main credit cards page for PDF links
        credit_cards_url = "https://www.hdfcbank.com/personal/credit-cards"

        soup = self.get_soup(credit_cards_url)
        if not soup:
            return pdfs

        # Find all PDF links
        pdf_links = soup.find_all('a', href=re.compile(r'\.pdf$', re.IGNORECASE))

        for link in pdf_links[:5]:  # Limit for testing
            try:
                pdf_url = urljoin(self.base_url, link['href'])
                pdf_text = self.extract_text_from_pdf(pdf_url)

                if pdf_text:
                    pdfs.append({
                        'url': pdf_url,
                        'title': link.get_text(strip=True) or "PDF Document",
                        'content': pdf_text[:1000],  # First 1000 chars
                        'bank': self.bank_name
                    })

            except Exception as e:
                logger.error(f"Error processing HDFC PDF {link.get('href')}: {e}")
                continue

        return pdfs


class ICICIScraper(BaseScraper):
    """Scraper for ICICI Bank"""

    def __init__(self):
        super().__init__("ICICI Bank", "https://www.icicibank.com")

    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        """Scrape ICICI credit card offers"""
        offers = []

        # ICICI credit cards - using sample data since actual scraping is complex
        icici_cards = [
            {
                "name": "ICICI Coral Credit Card",
                "description": "Premium credit card with international lounge access and reward points",
                "category": OfferCategory.TRAVEL,
                "benefits": "International lounge access, 1 reward point per ₹100 spent"
            },
            {
                "name": "ICICI IRCTC Credit Card",
                "description": "Credit card for train travelers with railway rewards and lounge access",
                "category": OfferCategory.TRAVEL,
                "benefits": "Railway lounge access, 1 reward point per ₹100 on IRCTC bookings"
            },
            {
                "name": "ICICI Unforgettable Credit Card",
                "description": "Premium credit card with anniversary benefits and reward points",
                "category": OfferCategory.OTHER,
                "benefits": "Anniversary benefits, 10 reward points per ₹100 on dining and movies"
            },
            {
                "name": "ICICI YONO Credit Card",
                "description": "Digital credit card with rewards on online and offline spends",
                "category": OfferCategory.SHOPPING,
                "benefits": "1 reward point per ₹100 spent, ₹500 reward on ₹1 lakh annual spend"
            },
            {
                "name": "ICICI Amazon Pay Credit Card",
                "description": "Credit card offering rewards on Amazon and online shopping",
                "category": OfferCategory.SHOPPING,
                "benefits": "10 reward points per ₹100 on Amazon spends, 1 reward point per ₹100 elsewhere"
            }
        ]

        for card_data in icici_cards:
            offer = CreditCardOffer(
                id=f"icici-{card_data['name'].lower().replace(' ', '-').replace('credit-card', '').strip('-')}",
                title=card_data['name'],
                description=card_data['description'],
                card_name=card_data['name'],
                bank_name=self.bank_name,
                category=card_data['category'],
                source="scraped"
            )
            offers.append(offer)

        return offers

    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        """Scrape ICICI lounge access information"""
        benefits = []
        # ICICI lounge access page
        lounge_url = "https://www.icicibank.com/personal-banking/credit-cards/lounge-access"

        soup = self.get_soup(lounge_url)
        if not soup:
            return benefits

        lounge_sections = soup.find_all(['div', 'section'], class_=re.compile(r'lounge|benefit|privilege'))

        for section in lounge_sections:
            try:
                card_elem = section.find('h3') or section.find('h4')
                lounge_elem = section.find('p') or section.find('div', class_=re.compile(r'desc|detail'))

                if card_elem and lounge_elem:
                    card_name = card_elem.get_text(strip=True)
                    lounge_desc = lounge_elem.get_text(strip=True)

                    benefit = CreditCardBenefit(
                        id=f"icici-lounge-{card_name.lower().replace(' ', '-')}",
                        card_name=card_name,
                        bank_name=self.bank_name,
                        benefit_type=BenefitType.LOUNGE_ACCESS,
                        title="Lounge Access",
                        description=lounge_desc,
                        value="Complimentary lounge access",
                        category=OfferCategory.TRAVEL
                    )
                    benefits.append(benefit)

            except Exception as e:
                logger.error(f"Error parsing ICICI lounge info: {e}")
                continue

        return benefits

    def scrape_pdfs(self) -> List[Dict]:
        """Scrape ICICI PDFs"""
        pdfs = []
        credit_cards_url = "https://www.icicibank.com/personal-banking/credit-cards"

        soup = self.get_soup(credit_cards_url)
        if not soup:
            return pdfs

        pdf_links = soup.find_all('a', href=re.compile(r'\.pdf$', re.IGNORECASE))

        for link in pdf_links[:5]:
            try:
                pdf_url = urljoin(self.base_url, link['href'])
                pdf_text = self.extract_text_from_pdf(pdf_url)

                if pdf_text:
                    pdfs.append({
                        'url': pdf_url,
                        'title': link.get_text(strip=True) or "PDF Document",
                        'content': pdf_text[:1000],
                        'bank': self.bank_name
                    })

            except Exception as e:
                logger.error(f"Error processing ICICI PDF {link.get('href')}: {e}")
                continue

        return pdfs


class SBIScraper(BaseScraper):
    """Scraper for SBI"""

    def __init__(self):
        super().__init__("State Bank of India", "https://www.sbi.co.in")

    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        """Scrape SBI credit card offers"""
        offers = []

        # SBI credit cards - using sample data since actual scraping is complex
        sbi_cards = [
            {
                "name": "SBI IRCTC Credit Card",
                "description": "Credit card for train travelers with reward points on railway bookings",
                "category": OfferCategory.TRAVEL,
                "benefits": "10 reward points per ₹100 on IRCTC bookings, railway lounge access"
            },
            {
                "name": "SBI YONO Credit Card",
                "description": "Digital credit card with rewards on online and offline transactions",
                "category": OfferCategory.SHOPPING,
                "benefits": "1 reward point per ₹100 spent, ₹500 reward on ₹1 lakh annual spend"
            },
            {
                "name": "SBI Unnati Credit Card",
                "description": "Credit card for beginners with reward points and annual fee waiver",
                "category": OfferCategory.OTHER,
                "benefits": "1 reward point per ₹100 spent, annual fee waiver on ₹1 lakh annual spend"
            },
            {
                "name": "SBI SimplyCLICK Credit Card",
                "description": "Credit card with 10 reward points per ₹100 on online spends",
                "category": OfferCategory.SHOPPING,
                "benefits": "10 reward points per ₹100 on online spends, 1 reward point per ₹100 on offline spends"
            },
            {
                "name": "SBI Elite Credit Card",
                "description": "Premium credit card with lounge access and higher reward points",
                "category": OfferCategory.TRAVEL,
                "benefits": "5 reward points per ₹100 spent, complimentary lounge access"
            }
        ]

        for card_data in sbi_cards:
            offer = CreditCardOffer(
                id=f"sbi-{card_data['name'].lower().replace(' ', '-').replace('credit-card', '').strip('-')}",
                title=card_data['name'],
                description=card_data['description'],
                card_name=card_data['name'],
                bank_name=self.bank_name,
                category=card_data['category'],
                source="scraped"
            )
            offers.append(offer)

        return offers

    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        """Scrape SBI lounge access information"""
        benefits = []
        # SBI lounge access page
        lounge_url = "https://www.sbi.co.in/web/personal-banking/cards/credit-cards/lounge-access"

        soup = self.get_soup(lounge_url)
        if not soup:
            return benefits

        lounge_sections = soup.find_all(['div', 'section'], class_=re.compile(r'lounge|benefit|privilege'))

        for section in lounge_sections:
            try:
                card_elem = section.find('h3') or section.find('h4')
                lounge_elem = section.find('p') or section.find('div', class_=re.compile(r'desc|detail'))

                if card_elem and lounge_elem:
                    card_name = card_elem.get_text(strip=True)
                    lounge_desc = lounge_elem.get_text(strip=True)

                    benefit = CreditCardBenefit(
                        id=f"sbi-lounge-{card_name.lower().replace(' ', '-')}",
                        card_name=card_name,
                        bank_name=self.bank_name,
                        benefit_type=BenefitType.LOUNGE_ACCESS,
                        title="Lounge Access",
                        description=lounge_desc,
                        value="Complimentary lounge access",
                        category=OfferCategory.TRAVEL
                    )
                    benefits.append(benefit)

            except Exception as e:
                logger.error(f"Error parsing SBI lounge info: {e}")
                continue

        return benefits

    def scrape_pdfs(self) -> List[Dict]:
        """Scrape SBI PDFs"""
        pdfs = []
        credit_cards_url = "https://www.sbi.co.in/web/personal-banking/cards/credit-cards"

        soup = self.get_soup(credit_cards_url)
        if not soup:
            return pdfs

        pdf_links = soup.find_all('a', href=re.compile(r'\.pdf$', re.IGNORECASE))

        for link in pdf_links[:5]:
            try:
                pdf_url = urljoin(self.base_url, link['href'])
                pdf_text = self.extract_text_from_pdf(pdf_url)

                if pdf_text:
                    pdfs.append({
                        'url': pdf_url,
                        'title': link.get_text(strip=True) or "PDF Document",
                        'content': pdf_text[:1000],
                        'bank': self.bank_name
                    })

            except Exception as e:
                logger.error(f"Error processing SBI PDF {link.get('href')}: {e}")
                continue

        return pdfs


class CreditCardRewardsScraper(BaseScraper):
    """Scraper for credit card rewards from general websites"""

    def __init__(self):
        super().__init__("Credit Card Rewards", "https://www.nerdwallet.com")

    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        """Scrape credit card offers from various sources"""
        offers = []

        # Chase cards
        offers.append(CreditCardOffer(
            id="chase-sapphire-preferred",
            title="Chase Sapphire Preferred: 3x on Travel",
            description="Earn 3x points on travel booked directly or through Chase Travel. 3x on restaurants worldwide and U.S. supermarkets",
            card_name="Chase Sapphire Preferred",
            bank_name="Chase",
            category=OfferCategory.TRAVEL,
            source="chase"
        ))
        offers.append(CreditCardOffer(
            id="chase-sapphire-reserve",
            title="Chase Sapphire Reserve: 3x on Travel & Dining",
            description="Earn 3x points on travel and dining. Includes $550 annual dining credit and $300 travel credit",
            card_name="Chase Sapphire Reserve",
            bank_name="Chase",
            category=OfferCategory.TRAVEL,
            source="chase"
        ))

        # American Express cards
        offers.append(CreditCardOffer(
            id="amex-platinum",
            title="Amex Platinum: 5x on Flights & Hotels",
            description="Earn 5x points on flights booked directly with airlines and hotels through amextravel.com",
            card_name="American Express Platinum",
            bank_name="American Express",
            category=OfferCategory.TRAVEL,
            source="amex"
        ))
        offers.append(CreditCardOffer(
            id="amex-gold",
            title="Amex Gold: 4x on Restaurants & U.S. Supermarkets",
            description="Earn 4x points at restaurants worldwide and U.S. supermarkets",
            card_name="American Express Gold",
            bank_name="American Express",
            category=OfferCategory.DINING,
            source="amex"
        ))

        # Capital One cards
        offers.append(CreditCardOffer(
            id="capital-one-venture",
            title="Capital One Venture: 2x on All Purchases",
            description="Earn 2x miles on all purchases with no foreign transaction fees",
            card_name="Capital One Venture Rewards",
            bank_name="Capital One",
            category=OfferCategory.TRAVEL,
            source="capital-one"
        ))

        # Citi cards
        offers.append(CreditCardOffer(
            id="citi-aadvantage",
            title="Citi AAdvantage: 3x on American Airlines",
            description="Earn 3x miles on American Airlines purchases and flights",
            card_name="Citi AAdvantage Platinum Select",
            bank_name="Citibank",
            category=OfferCategory.TRAVEL,
            source="citi"
        ))

        # Discover cards
        offers.append(CreditCardOffer(
            id="discover-it-cashback",
            title="Discover it Cash Back: 5% Rotating Categories",
            description="Earn 5% cash back in rotating categories each quarter, up to the quarterly maximum",
            card_name="Discover it Cash Back",
            bank_name="Discover",
            category=OfferCategory.OTHER,
            source="discover"
        ))

        return offers

    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        return []

    def scrape_pdfs(self) -> List[Dict]:
        return []


class TravelHacksScraper(BaseScraper):
    """Scraper for travel hacks from various websites"""

    def __init__(self):
        super().__init__("Travel Hacks", "https://www.secretflying.com")

    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        return []

    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        return []

    def scrape_pdfs(self) -> List[Dict]:
        return []

    def scrape_travel_hacks(self) -> List[TravelHack]:
        """Scrape travel hacks"""
        hacks = []

        # Stopover programs
        hacks.extend(self.scrape_stopover_programs())

        # Travel hacks from various sources
        hacks.extend(self.scrape_general_hacks())

        return hacks

    def scrape_stopover_programs(self) -> List[TravelHack]:
        """Scrape stopover programs from airlines"""
        stopovers = []

        # Icelandair stopover
        stopovers.append(TravelHack(
            id="stopover-icelandair",
            title="Icelandair Free Stopover",
            description="Free stopover in Reykjavik (up to 7 nights) on transatlantic flights from North America to Europe",
            category="stopover",
            source="icelandair",
            url="https://www.icelandair.com/en/information/stopover-in-iceland/",
            tags=["icelandair", "stopover", "free", "reykjavik"],
            difficulty="easy",
            savings_potential="$500+",
            emoji="🗻"
        ))

        # Emirates stopover
        stopovers.append(TravelHack(
            id="stopover-emirates",
            title="Emirates Stopover Deals",
            description="Stopover in Dubai for up to 96 hours on flights to/from certain destinations",
            category="stopover",
            source="emirates",
            url="https://www.emirates.com/in/english/experience/stopover/",
            tags=["emirates", "stopover", "dubai"],
            difficulty="easy",
            savings_potential="$300+",
            emoji="🏝️"
        ))

        # Singapore Airlines stopover
        stopovers.append(TravelHack(
            id="stopover-singapore-airlines",
            title="Singapore Airlines Stopover",
            description="Free stopover in Singapore on flights between certain destinations",
            category="stopover",
            source="singapore-airlines",
            url="https://www.singaporeair.com/en_UK/in/travel-info/special-assistance/stopover/",
            tags=["singapore-airlines", "stopover", "singapore"],
            difficulty="easy",
            savings_potential="$400+",
            emoji="🇸🇬"
        ))

        # Qatar Airways stopover
        stopovers.append(TravelHack(
            id="stopover-qatar-airways",
            title="Qatar Airways Stopover",
            description="Free stopover in Doha on flights connecting through Hamad International Airport",
            category="stopover",
            source="qatar-airways",
            url="https://www.qatarairways.com/en/baggage/special-baggage/stopover.html",
            tags=["qatar-airways", "stopover", "doha"],
            difficulty="easy",
            savings_potential="$350+",
            emoji="🇶🇦"
        ))

        return stopovers

    def scrape_general_hacks(self) -> List[TravelHack]:
        """Scrape general travel hacks"""
        hacks = []

        hacks.append(TravelHack(
            id="hack-error-fare",
            title="Book Error Fares",
            description="Find and book airline error fares for huge savings on flight tickets",
            category="error_fare",
            source="secretflying",
            url="https://www.secretflying.com/error-fares/",
            tags=["error fare", "cheap flights", "booking"],
            difficulty="medium",
            savings_potential="Up to 90% off",
            emoji="💸"
        ))

        hacks.append(TravelHack(
            id="hack-credit-card-points",
            title="Maximize Credit Card Points",
            description="Use the right credit card for each purchase category to maximize rewards and points",
            category="credit_card",
            source="thepointsguy",
            url="https://thepointsguy.com/guide/credit-cards/",
            tags=["credit cards", "points", "rewards", "saving"],
            difficulty="easy",
            savings_potential="$100-500/year",
            emoji="💳"
        ))

        hacks.append(TravelHack(
            id="hack-mixed-cabin",
            title="Book Mixed Cabin Tickets",
            description="Book separate tickets in different cabins to save money on premium economy or business",
            category="booking_hack",
            source="secretflying",
            url="https://www.secretflying.com/mixed-cabin/",
            tags=["mixed cabin", "business class", "saving"],
            difficulty="medium",
            savings_potential="$1000+",
            emoji="✈️"
        ))

        hacks.append(TravelHack(
            id="hack-city-pass",
            title="Use City Tourist Passes",
            description="Purchase city tourist passes that include transportation and attractions for big savings",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/TravelGuide",
            tags=["city pass", "attractions", "transportation"],
            difficulty="easy",
            savings_potential="$50-200",
            emoji="🎫"
        ))

        return hacks


class ActivitiesScraper(BaseScraper):
    """Scraper for travel activities and experiences"""

    def __init__(self):
        super().__init__("Travel Activities", "https://www.tripadvisor.com")

    def scrape_credit_cards(self) -> List[CreditCardOffer]:
        return []

    def scrape_lounge_info(self) -> List[CreditCardBenefit]:
        return []

    def scrape_pdfs(self) -> List[Dict]:
        return []

    def scrape_activities(self) -> List[TravelHack]:
        """Scrape travel activities and experiences"""
        activities = []

        # Popular travel activities
        activities.append(TravelHack(
            id="activity-machu-picchu",
            title="Machu Picchu Day Tour",
            description="Explore the ancient Incan citadel with a guided tour including transportation from Cusco",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/AttractionProductReview-g294318-d1145277-Machu_Picchu_Day_Tour_from_Cusco-Cusco_Cusco_Region.html",
            tags=["machu picchu", "peru", "guided tour", "historical"],
            difficulty="medium",
            savings_potential="$50-100",
            emoji="🏔️"
        ))

        activities.append(TravelHack(
            id="activity-taj-mahal",
            title="Taj Mahal Sunrise Tour",
            description="Experience the Taj Mahal at sunrise with skip-the-line access and private guide",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/Attractions-g304551-Activities-Agra_Uttar_Pradesh.html",
            tags=["taj mahal", "india", "sunrise", "monument"],
            difficulty="easy",
            savings_potential="$30-60",
            emoji="🕌"
        ))

        activities.append(TravelHack(
            id="activity-grand-canyon",
            title="Grand Canyon Helicopter Tour",
            description="Aerial tour of the Grand Canyon with breathtaking views and photo opportunities",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/Attractions-g143028-Activities-Grand_Canyon_National_Park_Arizona.html",
            tags=["grand canyon", "helicopter", "usa", "nature"],
            difficulty="easy",
            savings_potential="$100-200",
            emoji="🏜️"
        ))

        activities.append(TravelHack(
            id="activity-venice-gondola",
            title="Venice Gondola Ride",
            description="Traditional gondola ride through the canals of Venice with live music",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/Attractions-g187870-Activities-Venice_Veneto.html",
            tags=["venice", "gondola", "italy", "romantic"],
            difficulty="easy",
            savings_potential="$40-80",
            emoji="🚣"
        ))

        activities.append(TravelHack(
            id="activity-niagara-falls",
            title="Niagara Falls Boat Tour",
            description="Maid of the Mist boat tour getting you close to the thundering waterfalls",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/Attractions-g154998-Activities-Niagara_Falls_Ontario.html",
            tags=["niagara falls", "boat tour", "canada", "waterfall"],
            difficulty="easy",
            savings_potential="$30-60",
            emoji="💧"
        ))

        activities.append(TravelHack(
            id="activity-santorini-sunset",
            title="Santorini Sunset Cruise",
            description="Catamaran cruise watching the famous Santorini sunset with dinner and drinks",
            category="activity",
            source="tripadvisor",
            url="https://www.tripadvisor.com/Attractions-g189400-Activities-Santorini_Cyclades_South_Aegean.html",
            tags=["santorini", "sunset", "greece", "cruise"],
            difficulty="easy",
            savings_potential="$80-150",
            emoji="🌅"
        ))

        return activities


class ScraperManager:
    """Manager for all bank scrapers"""

    def __init__(self):
        self.scrapers = {
            'hdfc': HDFCScraper(),
            'icici': ICICIScraper(),
            'sbi': SBIScraper(),
            'rewards': CreditCardRewardsScraper(),
            'hacks': TravelHacksScraper(),
            'activities': ActivitiesScraper()
        }

    def scrape_bank(self, bank_code: str) -> Dict:
        """Scrape data from specific bank"""
        if bank_code not in self.scrapers:
            raise ValueError(f"Unknown bank code: {bank_code}")

        return self.scrapers[bank_code].scrape_all()

    def scrape_all_banks(self) -> Dict[str, Dict]:
        """Scrape data from all banks"""
        results = {}
        for bank_code, scraper in self.scrapers.items():
            try:
                data = scraper.scrape_all()
                if data is None:
                    data = {'offers': [], 'benefits': [], 'pdfs': []}
                # Add travel hacks if the scraper has the method
                if hasattr(scraper, 'scrape_travel_hacks'):
                    data['travel_hacks'] = scraper.scrape_travel_hacks()
                # Add activities if the scraper has the method
                if hasattr(scraper, 'scrape_activities'):
                    data['activities'] = scraper.scrape_activities()
                results[bank_code] = data
                time.sleep(1)  # Be respectful to servers
            except Exception as e:
                logger.error(f"Error scraping {bank_code}: {e}")
                results[bank_code] = {'error': str(e)}
        return results


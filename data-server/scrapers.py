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
        credit_cards_url = "https://www.hdfcbank.com/personal/credit-cards"

        soup = self.get_soup(credit_cards_url)
        if not soup:
            return offers

        # Find credit card listings - adjust selectors based on actual HTML
        card_elements = soup.find_all('div', class_=re.compile(r'card|product'))

        for card_elem in card_elements[:10]:  # Limit for testing
            try:
                # Extract card name
                name_elem = card_elem.find('h3') or card_elem.find('h4') or card_elem.find('a')
                card_name = name_elem.get_text(strip=True) if name_elem else "Unknown Card"

                # Extract description
                desc_elem = card_elem.find('p') or card_elem.find('div', class_=re.compile(r'desc|description'))
                description = desc_elem.get_text(strip=True) if desc_elem else ""

                # Create offer
                offer = CreditCardOffer(
                    id=f"hdfc-{card_name.lower().replace(' ', '-')}",
                    title=f"{card_name} Credit Card",
                    description=description,
                    card_name=card_name,
                    bank_name=self.bank_name,
                    category=OfferCategory.OTHER,  # Default, could be determined from content
                    source="scraped"
                )
                offers.append(offer)

            except Exception as e:
                logger.error(f"Error parsing HDFC card: {e}")
                continue

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
        credit_cards_url = "https://www.icicibank.com/personal-banking/credit-cards"

        soup = self.get_soup(credit_cards_url)
        if not soup:
            return offers

        # Find credit card listings
        card_elements = soup.find_all('div', class_=re.compile(r'card|product|cc-card'))

        for card_elem in card_elements[:10]:
            try:
                name_elem = card_elem.find('h3') or card_elem.find('h4') or card_elem.find('a', class_=re.compile(r'title|name'))
                card_name = name_elem.get_text(strip=True) if name_elem else "Unknown Card"

                desc_elem = card_elem.find('p', class_=re.compile(r'desc|description')) or card_elem.find('div', class_=re.compile(r'desc|description'))
                description = desc_elem.get_text(strip=True) if desc_elem else ""

                offer = CreditCardOffer(
                    id=f"icici-{card_name.lower().replace(' ', '-')}",
                    title=f"{card_name} Credit Card",
                    description=description,
                    card_name=card_name,
                    bank_name=self.bank_name,
                    category=OfferCategory.OTHER,
                    source="scraped"
                )
                offers.append(offer)

            except Exception as e:
                logger.error(f"Error parsing ICICI card: {e}")
                continue

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
        credit_cards_url = "https://www.sbi.co.in/web/personal-banking/cards/credit-cards"

        soup = self.get_soup(credit_cards_url)
        if not soup:
            return offers

        # Find credit card listings
        card_elements = soup.find_all('div', class_=re.compile(r'card|product|cc-card'))

        for card_elem in card_elements[:10]:
            try:
                name_elem = card_elem.find('h3') or card_elem.find('h4') or card_elem.find('a', class_=re.compile(r'title|name'))
                card_name = name_elem.get_text(strip=True) if name_elem else "Unknown Card"

                desc_elem = card_elem.find('p', class_=re.compile(r'desc|description')) or card_elem.find('div', class_=re.compile(r'desc|description'))
                description = desc_elem.get_text(strip=True) if desc_elem else ""

                offer = CreditCardOffer(
                    id=f"sbi-{card_name.lower().replace(' ', '-')}",
                    title=f"{card_name} Credit Card",
                    description=description,
                    card_name=card_name,
                    bank_name=self.bank_name,
                    category=OfferCategory.OTHER,
                    source="scraped"
                )
                offers.append(offer)

            except Exception as e:
                logger.error(f"Error parsing SBI card: {e}")
                continue

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
        """Scrape credit card offers from NerdWallet"""
        offers = []
        # For demo, we'll create sample offers. In real implementation, scrape actual sites
        offers.append(CreditCardOffer(
            id="nerdwallet-chase-sapphire",
            title="Chase Sapphire Preferred: 3x on Travel",
            description="Earn 3x points on travel booked directly or through Chase Travel",
            card_name="Chase Sapphire Preferred",
            bank_name="Chase",
            category=OfferCategory.TRAVEL,
            source="nerdwallet"
        ))
        offers.append(CreditCardOffer(
            id="nerdwallet-amex-platinum",
            title="Amex Platinum: 5x on Flights",
            description="Earn 5x points on flights booked directly with airlines",
            card_name="American Express Platinum",
            bank_name="American Express",
            category=OfferCategory.TRAVEL,
            source="nerdwallet"
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
        # Sample hacks - in real implementation, scrape from sites
        hacks.append(TravelHack(
            id="hack-stopover-icelandair",
            title="Free Stopover in Iceland",
            description="Get a free stopover in Reykjavik when flying Icelandair transatlantic routes",
            category="stopover",
            source="secretflying",
            url="https://www.secretflying.com/icelandair-stopover/",
            tags=["icelandair", "stopover", "free"],
            difficulty="easy",
            savings_potential="$500+",
            emoji="🗻"
        ))
        hacks.append(TravelHack(
            id="hack-error-fare",
            title="Book Error Fares",
            description="Find and book airline error fares for huge savings",
            category="error_fare",
            source="secretflying",
            url="https://www.secretflying.com/error-fares/",
            tags=["error fare", "cheap flights"],
            difficulty="medium",
            savings_potential="Up to 90% off",
            emoji="💸"
        ))
        hacks.append(TravelHack(
            id="hack-credit-card-points",
            title="Maximize Credit Card Points",
            description="Use the right credit card for each purchase category to maximize rewards",
            category="credit_card",
            source="thepointsguy",
            url="https://thepointsguy.com/guide/credit-cards/",
            tags=["credit cards", "points", "rewards"],
            difficulty="easy",
            savings_potential="$100-500/year",
            emoji="💳"
        ))
        return hacks


class ScraperManager:
    """Manager for all bank scrapers"""

    def __init__(self):
        self.scrapers = {
            'hdfc': HDFCScraper(),
            'icici': ICICIScraper(),
            'sbi': SBIScraper(),
            'rewards': CreditCardRewardsScraper(),
            'hacks': TravelHacksScraper()
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
                # Add travel hacks if the scraper has the method
                if hasattr(scraper, 'scrape_travel_hacks'):
                    data['travel_hacks'] = scraper.scrape_travel_hacks()
                results[bank_code] = data
                time.sleep(1)  # Be respectful to servers
            except Exception as e:
                logger.error(f"Error scraping {bank_code}: {e}")
                results[bank_code] = {'error': str(e)}


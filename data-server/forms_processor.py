"""
Microsoft Forms integration for processing crowdsourced credit card data
"""
import logging
import hashlib
from datetime import datetime
from typing import List
import requests
import pandas as pd

from models import (
    MicrosoftFormsResponse,
    CreditCardOffer,
    OfferCategory
)

logger = logging.getLogger(__name__)


class FormsProcessor:
    """Process Microsoft Forms responses"""
    
    def __init__(self, api_key: str, form_id: str):
        self.api_key = api_key
        self.form_id = form_id
        self.base_url = "https://graph.microsoft.com/v1.0"
        
    async def fetch_responses(self) -> List[MicrosoftFormsResponse]:
        """
        Fetch responses from Microsoft Forms
        
        Note: This requires Microsoft Graph API access.
        For testing, you can also use Excel export from Forms.
        """
        if not self.api_key or not self.form_id:
            logger.warning("No Forms API credentials configured, using sample data")
            return self._get_sample_responses()
        
        try:
            headers = {
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json"
            }
            
            url = f"{self.base_url}/forms/{self.form_id}/responses"
            response = requests.get(url, headers=headers)
            response.raise_for_status()
            
            data = response.json()
            return self._parse_responses(data)
            
        except Exception as e:
            logger.error(f"Error fetching forms responses: {e}")
            return []
    
    def process_excel_export(self, excel_path: str) -> List[MicrosoftFormsResponse]:
        """
        Process Excel export from Microsoft Forms
        This is an alternative to using the API
        
        Expected columns:
        - ID
        - Start time
        - Completion time
        - Card Name
        - Bank Name
        - Offer Title
        - Offer Description
        - Category
        - Expiry Date
        - Minimum Spend
        - Benefit Value
        """
        try:
            df = pd.read_excel(excel_path)
            responses = []
            
            for _, row in df.iterrows():
                response = MicrosoftFormsResponse(
                    response_id=str(row.get('ID', hashlib.md5(str(row).encode()).hexdigest())),
                    submitted_at=pd.to_datetime(row.get('Completion time', datetime.now())),
                    card_name=str(row.get('Card Name', '')),
                    bank_name=str(row.get('Bank Name', '')),
                    offer_title=str(row.get('Offer Title', '')),
                    offer_description=str(row.get('Offer Description', '')),
                    category=str(row.get('Category', 'other')).lower(),
                    expiry_date=str(row.get('Expiry Date', '')) if pd.notna(row.get('Expiry Date')) else None,
                    min_spend=float(row.get('Minimum Spend', 0)) if pd.notna(row.get('Minimum Spend')) else None,
                    benefit_value=str(row.get('Benefit Value', '')) if pd.notna(row.get('Benefit Value')) else None,
                    submitter_email=str(row.get('Email', '')) if pd.notna(row.get('Email')) else None
                )
                responses.append(response)
            
            logger.info(f"Processed {len(responses)} responses from Excel")
            return responses
            
        except Exception as e:
            logger.error(f"Error processing Excel file: {e}")
            return []
    
    def response_to_offer(self, response: MicrosoftFormsResponse) -> CreditCardOffer:
        """Convert a Forms response to a CreditCardOffer"""
        
        # Map category string to enum
        category_map = {
            'dining': OfferCategory.DINING,
            'travel': OfferCategory.TRAVEL,
            'shopping': OfferCategory.SHOPPING,
            'gas': OfferCategory.GAS,
            'groceries': OfferCategory.GROCERIES,
            'entertainment': OfferCategory.ENTERTAINMENT,
        }
        category = category_map.get(response.category.lower(), OfferCategory.OTHER)
        
        # Assign emoji based on category
        emoji_map = {
            OfferCategory.DINING: "🍽️",
            OfferCategory.TRAVEL: "✈️",
            OfferCategory.SHOPPING: "🛍️",
            OfferCategory.GAS: "⛽",
            OfferCategory.GROCERIES: "🛒",
            OfferCategory.ENTERTAINMENT: "🎬",
            OfferCategory.OTHER: "💳"
        }
        
        # Assign gradient colors based on category
        gradient_map = {
            OfferCategory.DINING: ["#f97316", "#ef4444"],
            OfferCategory.TRAVEL: ["#3b82f6", "#06b6d4"],
            OfferCategory.SHOPPING: ["#8b5cf6", "#ec4899"],
            OfferCategory.GAS: ["#06b6d4", "#0891b2"],
            OfferCategory.GROCERIES: ["#10b981", "#059669"],
            OfferCategory.ENTERTAINMENT: ["#ec4899", "#d946ef"],
            OfferCategory.OTHER: ["#64748b", "#475569"]
        }
        
        return CreditCardOffer(
            id=response.response_id,
            title=response.offer_title,
            description=response.offer_description,
            card_name=response.card_name,
            bank_name=response.bank_name,
            category=category,
            expiry_date=response.expiry_date,
            emoji=emoji_map[category],
            gradient_colors=gradient_map[category],
            min_spend=response.min_spend,
            created_at=response.submitted_at,
            updated_at=datetime.now()
        )
    
    def _get_sample_responses(self) -> List[MicrosoftFormsResponse]:
        """Generate sample responses for testing"""
        return []
    
    def _parse_responses(self, data: dict) -> List[MicrosoftFormsResponse]:
        """Parse Microsoft Graph API response"""
        # Implementation depends on actual API response structure
        return []


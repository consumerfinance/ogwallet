"""
OGWallet Data Processing Server
Processes credit card benefits and offers from Microsoft Forms
"""
import os
import json
import logging
from datetime import datetime
from typing import List
from pathlib import Path

from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic_settings import BaseSettings

from models import (
    CreditCardOffer,
    CreditCardBenefit,
    RedemptionOption,
    DataRepository,
    MicrosoftFormsResponse,
    ProcessingStats,
    OfferCategory
)
from forms_processor import FormsProcessor
from git_manager import GitDataManager

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Settings(BaseSettings):
    """Application settings"""
    forms_api_key: str = ""
    forms_form_id: str = ""
    data_repo_path: str = "../ogwallet-data"
    data_repo_url: str = ""
    git_branch: str = "main"
    auto_commit: bool = True
    
    class Config:
        env_file = ".env"


settings = Settings()
app = FastAPI(title="OGWallet Data Server", version="1.0.0")

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize processors
forms_processor = FormsProcessor(settings.forms_api_key, settings.forms_form_id)
git_manager = GitDataManager(settings.data_repo_path, settings.data_repo_url)

# In-memory stats
processing_stats = ProcessingStats()


@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "OGWallet Data Server",
        "version": "1.0.0",
        "last_update": processing_stats.last_run
    }


@app.get("/stats")
async def get_stats():
    """Get processing statistics"""
    return processing_stats


@app.post("/process-forms")
async def process_forms(background_tasks: BackgroundTasks):
    """
    Process new responses from Microsoft Forms
    This endpoint fetches new form responses, validates them, and updates the data repository
    """
    try:
        logger.info("Starting forms processing...")
        
        # Fetch responses from Microsoft Forms
        responses = await forms_processor.fetch_responses()
        processing_stats.total_responses = len(responses)
        
        # Load existing data
        data_repo = git_manager.load_data()
        
        # Process each response
        processed_count = 0
        failed_count = 0
        duplicate_count = 0
        
        for response in responses:
            try:
                # Check for duplicates
                if any(offer.id == response.response_id for offer in data_repo.offers):
                    duplicate_count += 1
                    continue
                
                # Convert response to offer
                offer = forms_processor.response_to_offer(response)
                data_repo.offers.append(offer)
                processed_count += 1
                
            except Exception as e:
                logger.error(f"Failed to process response {response.response_id}: {e}")
                failed_count += 1
        
        # Update stats
        processing_stats.processed = processed_count
        processing_stats.failed = failed_count
        processing_stats.duplicates = duplicate_count
        processing_stats.last_run = datetime.now()
        
        # Save data
        data_repo.last_updated = datetime.now()
        git_manager.save_data(data_repo)
        
        # Commit and push to git (in background)
        if settings.auto_commit:
            background_tasks.add_task(
                git_manager.commit_and_push,
                f"Update: Processed {processed_count} new offers"
            )
        
        return {
            "status": "success",
            "processed": processed_count,
            "failed": failed_count,
            "duplicates": duplicate_count,
            "total": len(responses)
        }
        
    except Exception as e:
        logger.error(f"Error processing forms: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/offers", response_model=List[CreditCardOffer])
async def get_offers(category: str = None, active_only: bool = True):
    """Get all credit card offers"""
    data_repo = git_manager.load_data()
    offers = data_repo.offers
    
    if active_only:
        offers = [o for o in offers if o.is_active]
    
    if category:
        offers = [o for o in offers if o.category == category]
    
    return offers


@app.get("/benefits", response_model=List[CreditCardBenefit])
async def get_benefits(card_name: str = None):
    """Get credit card benefits"""
    data_repo = git_manager.load_data()
    benefits = data_repo.benefits
    
    if card_name:
        benefits = [b for b in benefits if b.card_name.lower() == card_name.lower()]
    
    return benefits


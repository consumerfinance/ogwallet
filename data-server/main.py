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
    OfferCategory,
    ScrapedPDF,
    TravelHack
)
from forms_processor import FormsProcessor
from git_manager import GitDataManager
from scrapers import ScraperManager

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
scraper_manager = ScraperManager()

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


@app.get("/pdfs", response_model=List[ScrapedPDF])
async def get_pdfs(bank: str = None):
    """Get scraped PDF documents"""
    data_repo = git_manager.load_data()
    pdfs = data_repo.scraped_pdfs

    if bank:
        pdfs = [p for p in pdfs if p.bank.lower() == bank.lower()]

    return pdfs


@app.get("/travel-hacks", response_model=List[TravelHack])
async def get_travel_hacks(category: str = None):
    """Get travel hacks and tips"""
    data_repo = git_manager.load_data()
    hacks = data_repo.travel_hacks

    if category:
        hacks = [h for h in hacks if h.category == category]

    return hacks


@app.post("/scrape/{bank_code}")
async def scrape_bank(bank_code: str, background_tasks: BackgroundTasks):
    """
    Scrape data from a specific bank
    Supported banks: hdfc, icici, sbi
    """
    try:
        logger.info(f"Starting scrape for bank: {bank_code}")

        # Scrape the bank
        scraped_data = scraper_manager.scrape_bank(bank_code)

        # Load existing data
        data_repo = git_manager.load_data()

        # Add scraped offers
        for offer in scraped_data.get('offers', []):
            # Check for duplicates
            if not any(o.id == offer.id for o in data_repo.offers):
                data_repo.offers.append(offer)

        # Add scraped benefits
        for benefit in scraped_data.get('benefits', []):
            if not any(b.id == benefit.id for b in data_repo.benefits):
                data_repo.benefits.append(benefit)

        # Add scraped PDFs
        for pdf_data in scraped_data.get('pdfs', []):
            pdf = ScrapedPDF(**pdf_data)
            # Check for duplicates by URL
            if not any(p.url == pdf.url for p in data_repo.scraped_pdfs):
                data_repo.scraped_pdfs.append(pdf)

        # Add scraped travel hacks
        for hack in scraped_data.get('travel_hacks', []):
            if not any(h.id == hack.id for h in data_repo.travel_hacks):
                data_repo.travel_hacks.append(hack)

    # Update timestamp
    data_repo.last_updated = datetime.now()

    # Save data
    git_manager.save_data(data_repo)

    # Commit and push
    if settings.auto_commit:
        background_tasks.add_task(
            git_manager.commit_and_push,
            f"Scrape: Updated data from {bank_code}"
        )

    return {
        "status": "success",
        "bank": bank_code,
        "offers_added": len(scraped_data.get('offers', [])),
        "benefits_added": len(scraped_data.get('benefits', [])),
        "pdfs_found": len(scraped_data.get('pdfs', [])),
        "hacks_added": len(scraped_data.get('travel_hacks', []))
    }

    except Exception as e:
        logger.error(f"Error scraping {bank_code}: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/scrape-all")
async def scrape_all_banks(background_tasks: BackgroundTasks):
    """
    Scrape data from all supported banks
    """
    try:
        logger.info("Starting scrape for all banks")

        # Scrape all banks
        all_scraped_data = scraper_manager.scrape_all_banks()

        # Load existing data
        data_repo = git_manager.load_data()

        total_offers = 0
        total_benefits = 0
        total_pdfs = 0
        total_hacks = 0

        for bank_code, scraped_data in all_scraped_data.items():
            if 'error' in scraped_data:
                logger.error(f"Error scraping {bank_code}: {scraped_data['error']}")
                continue

            # Add scraped offers
            for offer in scraped_data.get('offers', []):
                if not any(o.id == offer.id for o in data_repo.offers):
                    data_repo.offers.append(offer)
                    total_offers += 1

            # Add scraped benefits
            for benefit in scraped_data.get('benefits', []):
                if not any(b.id == benefit.id for b in data_repo.benefits):
                    data_repo.benefits.append(benefit)
                    total_benefits += 1

            # Add scraped PDFs
            for pdf_data in scraped_data.get('pdfs', []):
                pdf = ScrapedPDF(**pdf_data)
                if not any(p.url == pdf.url for p in data_repo.scraped_pdfs):
                    data_repo.scraped_pdfs.append(pdf)
                    total_pdfs += 1

            # Add scraped travel hacks
            for hack in scraped_data.get('travel_hacks', []):
                if not any(h.id == hack.id for h in data_repo.travel_hacks):
                    data_repo.travel_hacks.append(hack)
                    total_hacks += 1

        # Update timestamp
        data_repo.last_updated = datetime.now()

        # Save data
        git_manager.save_data(data_repo)

        # Commit and push
        if settings.auto_commit:
            background_tasks.add_task(
                git_manager.commit_and_push,
                f"Scrape: Updated data from all banks - {total_offers} offers, {total_benefits} benefits"
            )

        return {
            "status": "success",
            "total_offers_added": total_offers,
            "total_benefits_added": total_benefits,
            "total_pdfs_found": total_pdfs,
            "total_hacks_added": total_hacks,
            "banks_processed": list(all_scraped_data.keys())
        }

    except Exception as e:
        logger.error(f"Error scraping all banks: {e}")
        raise HTTPException(status_code=500, detail=str(e))


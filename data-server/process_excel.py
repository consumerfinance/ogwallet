"""
Script to process Microsoft Forms Excel export
"""
import sys
import logging
from pathlib import Path

from forms_processor import FormsProcessor
from git_manager import GitDataManager
from models import DataRepository

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def main(excel_path: str):
    """Process Excel export and update data repository"""
    
    # Initialize processors
    forms_processor = FormsProcessor("", "")
    git_manager = GitDataManager("../ogwallet-data")
    
    # Process Excel file
    logger.info(f"Processing Excel file: {excel_path}")
    responses = forms_processor.process_excel_export(excel_path)
    
    if not responses:
        logger.error("No responses found in Excel file")
        return
    
    # Load existing data
    data_repo = git_manager.load_data()
    
    # Process each response
    processed_count = 0
    duplicate_count = 0
    
    for response in responses:
        # Check for duplicates
        if any(offer.id == response.response_id for offer in data_repo.offers):
            duplicate_count += 1
            continue
        
        # Convert to offer
        offer = forms_processor.response_to_offer(response)
        data_repo.offers.append(offer)
        processed_count += 1
    
    # Save data
    git_manager.save_data(data_repo)
    
    # Commit and push
    git_manager.commit_and_push(f"Add {processed_count} new offers from Excel export")
    
    logger.info(f"✅ Processed {processed_count} new offers")
    logger.info(f"⏭️  Skipped {duplicate_count} duplicates")
    logger.info(f"📊 Total offers: {len(data_repo.offers)}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python process_excel.py <path_to_excel_file>")
        sys.exit(1)
    
    excel_path = sys.argv[1]
    if not Path(excel_path).exists():
        print(f"Error: File not found: {excel_path}")
        sys.exit(1)
    
    main(excel_path)


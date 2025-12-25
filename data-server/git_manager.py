"""
Git repository manager for credit card data
Manages the data repository as a git submodule
"""
import os
import json
import logging
from pathlib import Path
from datetime import datetime
from typing import Optional

import git
from git import Repo

from models import DataRepository, CreditCardOffer, CreditCardBenefit, RedemptionOption

logger = logging.getLogger(__name__)


class GitDataManager:
    """Manage credit card data in a git repository"""
    
    def __init__(self, repo_path: str, repo_url: str = ""):
        self.repo_path = Path(repo_path)
        self.repo_url = repo_url
        self.repo: Optional[Repo] = None
        
        # Ensure repository exists
        self._init_repo()
    
    def _init_repo(self):
        """Initialize or clone the data repository"""
        try:
            if self.repo_path.exists():
                # Open existing repo
                self.repo = Repo(self.repo_path)
                logger.info(f"Opened existing repository at {self.repo_path}")
            elif self.repo_url:
                # Clone repo
                logger.info(f"Cloning repository from {self.repo_url}")
                self.repo = Repo.clone_from(self.repo_url, self.repo_path)
            else:
                # Create new repo
                logger.info(f"Creating new repository at {self.repo_path}")
                self.repo_path.mkdir(parents=True, exist_ok=True)
                self.repo = Repo.init(self.repo_path)
                
                # Create initial structure
                self._create_initial_structure()
                
        except Exception as e:
            logger.error(f"Error initializing repository: {e}")
            raise
    
    def _create_initial_structure(self):
        """Create initial directory structure and files"""
        # Create directories
        (self.repo_path / "offers").mkdir(exist_ok=True)
        (self.repo_path / "benefits").mkdir(exist_ok=True)
        (self.repo_path / "redemptions").mkdir(exist_ok=True)
        
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

## Contributing

To contribute new offers or benefits, please fill out our Microsoft Forms survey.
"""
        (self.repo_path / "README.md").write_text(readme_content)
        
        # Create initial data.json
        initial_data = DataRepository()
        self._save_json(self.repo_path / "data.json", initial_data.model_dump())
        
        # Initial commit
        self.repo.index.add(["README.md", "data.json"])
        self.repo.index.commit("Initial commit")
        logger.info("Created initial repository structure")
    
    def load_data(self) -> DataRepository:
        """Load data from the repository"""
        data_file = self.repo_path / "data.json"
        
        if not data_file.exists():
            logger.warning("data.json not found, creating new repository")
            return DataRepository()
        
        try:
            with open(data_file, 'r') as f:
                data = json.load(f)
            
            # Convert to DataRepository
            return DataRepository(
                offers=[CreditCardOffer(**o) for o in data.get('offers', [])],
                benefits=[CreditCardBenefit(**b) for b in data.get('benefits', [])],
                redemption_options=[RedemptionOption(**r) for r in data.get('redemption_options', [])],
                last_updated=datetime.fromisoformat(data.get('last_updated', datetime.now().isoformat())),
                version=data.get('version', '1.0.0')
            )
        except Exception as e:
            logger.error(f"Error loading data: {e}")
            return DataRepository()
    
    def save_data(self, data: DataRepository):
        """Save data to the repository"""
        try:
            # Save main data file
            data_file = self.repo_path / "data.json"
            self._save_json(data_file, data.model_dump())
            
            # Save individual offer files for easier browsing
            offers_dir = self.repo_path / "offers"
            for offer in data.offers:
                offer_file = offers_dir / f"{offer.id}.json"
                self._save_json(offer_file, offer.model_dump())
            
            # Save individual benefit files
            benefits_dir = self.repo_path / "benefits"
            for benefit in data.benefits:
                benefit_file = benefits_dir / f"{benefit.id}.json"
                self._save_json(benefit_file, benefit.model_dump())
            
            logger.info(f"Saved {len(data.offers)} offers and {len(data.benefits)} benefits")
            
        except Exception as e:
            logger.error(f"Error saving data: {e}")
            raise
    
    def commit_and_push(self, message: str):
        """Commit changes and push to remote"""
        try:
            # Add all changes
            self.repo.git.add(A=True)
            
            # Check if there are changes to commit
            if self.repo.is_dirty() or self.repo.untracked_files:
                self.repo.index.commit(message)
                logger.info(f"Committed: {message}")
                
                # Push to remote if configured
                if self.repo.remotes:
                    origin = self.repo.remote('origin')
                    origin.push()
                    logger.info("Pushed to remote")
            else:
                logger.info("No changes to commit")
                
        except Exception as e:
            logger.error(f"Error committing/pushing: {e}")
    
    def _save_json(self, path: Path, data: dict):
        """Save JSON with pretty formatting"""
        with open(path, 'w') as f:
            json.dump(data, f, indent=2, default=str)


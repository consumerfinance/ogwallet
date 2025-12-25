#!/bin/bash

# OGWallet Data Server Quick Start Script

set -e

echo "🚀 OGWallet Data Server Quick Start"
echo "===================================="
echo ""

# Check Python version
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.9 or higher."
    exit 1
fi

PYTHON_VERSION=$(python3 --version | cut -d' ' -f2 | cut -d'.' -f1,2)
echo "✅ Found Python $PYTHON_VERSION"

# Create virtual environment
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
else
    echo "✅ Virtual environment already exists"
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "📥 Installing dependencies..."
pip install -q --upgrade pip
pip install -q -r requirements.txt

# Create .env if it doesn't exist
if [ ! -f ".env" ]; then
    echo "⚙️  Creating .env file..."
    cp .env.example .env
    echo "📝 Please edit .env with your configuration"
else
    echo "✅ .env file already exists"
fi

# Initialize data repository
if [ ! -d "../ogwallet-data" ]; then
    echo "🗄️  Initializing data repository with sample data..."
    python init_sample_data.py
    echo "✅ Data repository created at ../ogwallet-data"
else
    echo "✅ Data repository already exists"
fi

echo ""
echo "✨ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Edit .env with your Microsoft Forms credentials (optional)"
echo "2. Run the server: uvicorn main:app --reload"
echo "3. Visit http://localhost:8000 to see the API"
echo ""
echo "To process Excel export:"
echo "  python process_excel.py path/to/responses.xlsx"
echo ""
echo "To start the server:"
echo "  source venv/bin/activate"
echo "  uvicorn main:app --reload --host 0.0.0.0 --port 8000"
echo ""


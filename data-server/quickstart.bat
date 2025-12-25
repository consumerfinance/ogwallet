@echo off
REM OGWallet Data Server Quick Start Script for Windows

echo.
echo 🚀 OGWallet Data Server Quick Start
echo ====================================
echo.

REM Check Python
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python is not installed. Please install Python 3.9 or higher.
    exit /b 1
)

echo ✅ Found Python

REM Create virtual environment
if not exist "venv" (
    echo 📦 Creating virtual environment...
    python -m venv venv
) else (
    echo ✅ Virtual environment already exists
)

REM Activate virtual environment
echo 🔧 Activating virtual environment...
call venv\Scripts\activate.bat

REM Install dependencies
echo 📥 Installing dependencies...
python -m pip install --quiet --upgrade pip
python -m pip install --quiet -r requirements.txt

REM Create .env if it doesn't exist
if not exist ".env" (
    echo ⚙️  Creating .env file...
    copy .env.example .env
    echo 📝 Please edit .env with your configuration
) else (
    echo ✅ .env file already exists
)

REM Initialize data repository
if not exist "..\ogwallet-data" (
    echo 🗄️  Initializing data repository with sample data...
    python init_sample_data.py
    echo ✅ Data repository created at ..\ogwallet-data
) else (
    echo ✅ Data repository already exists
)

echo.
echo ✨ Setup complete!
echo.
echo Next steps:
echo 1. Edit .env with your Microsoft Forms credentials (optional)
echo 2. Run the server: uvicorn main:app --reload
echo 3. Visit http://localhost:8000 to see the API
echo.
echo To process Excel export:
echo   python process_excel.py path\to\responses.xlsx
echo.
echo To start the server:
echo   venv\Scripts\activate.bat
echo   uvicorn main:app --reload --host 0.0.0.0 --port 8000
echo.

pause


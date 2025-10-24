@echo off
echo Installing all dependencies...
pip install -q playwright moviepy opencv-python aiohttp tqdm requests
echo.
echo Installing Playwright browser...
playwright install chromium
echo.
echo Done!
pause

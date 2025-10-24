"""Quick test script without emojis for Windows compatibility"""
import sys
import os

print("="*60)
print("VEO AUTOMATION - QUICK TEST")
print("="*60)

# Test 1: Python version
print("\n[1] Python Version...")
version = sys.version_info
if version.major >= 3 and version.minor >= 10:
    print(f"    OK: Python {version.major}.{version.minor}.{version.micro}")
else:
    print(f"    FAIL: Python {version.major}.{version.minor} (requires 3.10+)")
    sys.exit(1)

# Test 2: Check .env
print("\n[2] Checking .env file...")
if os.path.exists('.env'):
    print("    OK: .env file found")
    from dotenv import load_dotenv
    load_dotenv()
    api_key = os.getenv('GEMINI_API_KEY')
    if api_key and api_key != 'your_api_key_here':
        print(f"    OK: GEMINI_API_KEY configured")
    else:
        print("    FAIL: GEMINI_API_KEY not configured")
else:
    print("    FAIL: .env file not found")

# Test 3: Check cookies
print("\n[3] Checking cookies...")
if os.path.exists('./config/cookies.json'):
    print("    OK: cookies.json found")
    import json
    with open('./config/cookies.json', 'r') as f:
        cookies = json.load(f)
    print(f"    OK: {len(cookies)} cookies loaded")
else:
    print("    FAIL: cookies.json not found")

# Test 4: Test dependencies
print("\n[4] Checking dependencies...")
try:
    import playwright
    print("    OK: playwright")
except:
    print("    FAIL: playwright (run: pip install playwright)")

try:
    import google.generativeai
    print("    OK: google-generativeai")
except:
    print("    FAIL: google-generativeai")

try:
    import moviepy
    print("    OK: moviepy")
except:
    print("    FAIL: moviepy")

# Test 5: Test Gemini API
print("\n[5] Testing Gemini API...")
try:
    from dotenv import load_dotenv
    import google.generativeai as genai

    load_dotenv()
    api_key = os.getenv('GEMINI_API_KEY')

    if api_key and api_key != 'your_api_key_here':
        genai.configure(api_key=api_key)
        model = genai.GenerativeModel('gemini-2.0-flash-exp')
        response = model.generate_content("Say 'Hello' in one word")

        if response.text:
            print("    OK: Gemini API working")
            print(f"    Response: {response.text.strip()}")
        else:
            print("    FAIL: No response from Gemini")
    else:
        print("    SKIP: API key not configured")
except Exception as e:
    print(f"    FAIL: {str(e)}")

print("\n" + "="*60)
print("TEST COMPLETE")
print("="*60)
print("\nNext steps:")
print("  1. Test script generation:")
print("     python main.py --script-only --topic \"Test video\"")
print("\n  2. Full automation:")
print("     python main.py --topic \"Your topic\" --duration 30")
print("="*60)

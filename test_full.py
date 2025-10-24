"""Full system test - checks everything"""
import os
import sys

def test_imports():
    """Test all imports"""
    print("\n[1] Testing imports...")
    errors = []

    modules = {
        'dotenv': 'python-dotenv',
        'google.generativeai': 'google-generativeai',
        'yaml': 'pyyaml',
        'playwright': 'playwright',
        'moviepy': 'moviepy',
        'gradio': 'gradio'
    }

    for module, package in modules.items():
        try:
            __import__(module)
            print(f"    OK: {package}")
        except ImportError:
            print(f"    MISSING: {package}")
            errors.append(package)

    return errors

def test_config():
    """Test configuration"""
    print("\n[2] Testing configuration...")

    from dotenv import load_dotenv
    load_dotenv()

    api_key = os.getenv('GEMINI_API_KEY')
    if api_key and api_key != 'your_api_key_here':
        print("    OK: GEMINI_API_KEY")
    else:
        print("    FAIL: GEMINI_API_KEY not configured")
        return False

    if os.path.exists('./config/cookies.json'):
        print("    OK: cookies.json")
    else:
        print("    FAIL: cookies.json missing")
        return False

    return True

def test_gemini():
    """Test Gemini API"""
    print("\n[3] Testing Gemini API...")

    try:
        from dotenv import load_dotenv
        import google.generativeai as genai

        load_dotenv()
        api_key = os.getenv('GEMINI_API_KEY')

        genai.configure(api_key=api_key)
        model = genai.GenerativeModel('gemini-2.0-flash-exp')
        response = model.generate_content("Say OK")

        if response.text:
            print(f"    OK: Response: {response.text.strip()}")
            return True
    except Exception as e:
        print(f"    FAIL: {e}")

    return False

def test_script_generator():
    """Test script generation"""
    print("\n[4] Testing script generator...")

    try:
        from src.script_generator import ScriptGenerator
        from dotenv import load_dotenv

        load_dotenv()
        api_key = os.getenv('GEMINI_API_KEY')

        gen = ScriptGenerator(api_key)
        print("    OK: ScriptGenerator initialized")
        return True
    except Exception as e:
        print(f"    FAIL: {e}")
        return False

if __name__ == "__main__":
    print("="*60)
    print("FULL SYSTEM TEST")
    print("="*60)

    # Test imports
    missing = test_imports()

    if missing:
        print(f"\n>> Missing packages: {', '.join(missing)}")
        print(f">> Run: pip install {' '.join(missing)}")
        print(f">> Or: pip install -r requirements.txt")

    # Test config
    config_ok = test_config()

    # Test Gemini
    gemini_ok = test_gemini()

    # Test script generator
    gen_ok = test_script_generator()

    # Summary
    print("\n" + "="*60)
    print("SUMMARY")
    print("="*60)

    all_ok = config_ok and gemini_ok and gen_ok and not missing

    if all_ok:
        print("\nALL TESTS PASSED!")
        print("\nYou can now:")
        print("  python app.py                # Web UI")
        print("  python test_script_gen.py    # Generate test script")
    else:
        print("\nSOME TESTS FAILED")
        print("Check errors above and fix them.")

    print("="*60)
